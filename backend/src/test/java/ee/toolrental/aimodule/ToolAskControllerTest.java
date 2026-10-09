package ee.toolrental.aimodule;

import ee.toolrental.infrastructure.security.AppUserOidcService;
import ee.toolrental.infrastructure.security.AppUserPrincipal;
import ee.toolrental.infrastructure.RestExceptionHandler;
import ee.toolrental.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.hamcrest.Matchers;

import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ToolAskController.class)
@Import({SecurityConfig.class, RestExceptionHandler.class})
@TestPropertySource(properties = {
        "spring.security.oauth2.client.registration.google.client-id=test-client-id",
        "spring.security.oauth2.client.registration.google.client-secret=test-client-secret",
        "spring.ai.google.genai.api-key=test-api-key"})
class ToolAskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ToolAskService toolAskService;

    @MockitoBean
    private AppUserOidcService appUserOidcService;

    @Test
    void guestCannotUseAssistant() throws Exception {
        mockMvc.perform(post("/api/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Millised tööriistad on saadaval Kristiines?\"}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(toolAskService);
    }

    @Test
    void loggedInUserReceivesOnlyPublicToolFields() throws Exception {
        String question = "Millised tööriistad on saadaval Kristiines?";
        when(toolAskService.ask(question)).thenReturn(new ToolAskResponse(
                List.of(
                        new AvailableTool(12, "Akutrell", "Puurid", "Kristiine"),
                        new AvailableTool(15, "Matkatelk", "Matkamine", null))));

        OidcIdToken token = OidcIdToken.withTokenValue("test-token")
                .subject("test-user")
                .claim("email", "user@example.com")
                .build();
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_customer"));
        var principal = new AppUserPrincipal(1, authorities, token, null);
        var authentication = new OAuth2AuthenticationToken(principal, authorities, "google");

        mockMvc.perform(post("/api/ai/ask")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Millised tööriistad on saadaval Kristiines?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").doesNotExist())
                .andExpect(jsonPath("$.tools[0].toolId").value(12))
                .andExpect(jsonPath("$.tools[0].name").value("Akutrell"))
                .andExpect(jsonPath("$.tools[0].category").value("Puurid"))
                .andExpect(jsonPath("$.tools[0].location").value("Kristiine"))
                .andExpect(jsonPath("$.tools[0].city").doesNotExist())
                .andExpect(jsonPath("$.tools[0].district").doesNotExist())
                .andExpect(jsonPath("$.tools[0].email").doesNotExist())
                .andExpect(jsonPath("$.tools[1].location").value(Matchers.nullValue()));
    }

    @Test
    void loggedInUserReceivesEmptyToolsArrayWhenThereAreNoMatches() throws Exception {
        String question = "Millised tööriistad on saadaval Kristiines?";
        when(toolAskService.ask(question)).thenReturn(new ToolAskResponse(List.of()));

        OidcIdToken token = OidcIdToken.withTokenValue("test-token")
                .subject("test-user")
                .build();
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_customer"));
        var principal = new AppUserPrincipal(1, authorities, token, null);
        var authToken = new OAuth2AuthenticationToken(principal, authorities, "google");

        mockMvc.perform(post("/api/ai/ask")
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Millised tööriistad on saadaval Kristiines?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").doesNotExist())
                .andExpect(jsonPath("$.tools").isArray())
                .andExpect(jsonPath("$.tools").isEmpty());
    }

    @Test
    void invalidQuestionReturns400ApiErrorWithoutCallingService() throws Exception {
        mockMvc.perform(post("/api/ai/ask")
                        .with(loggedInUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INCORRECT_INPUT"))
                .andExpect(jsonPath("$.message").exists());
        verifyNoInteractions(toolAskService);
    }

    @Test
    void unsupportedQuestionReturns422WithDedicatedSafeApiError() throws Exception {
        String question = "Kui palju maksab laenutus?";
        when(toolAskService.ask(question)).thenThrow(new UnsupportedToolSearchQuestionException());

        mockMvc.perform(post("/api/ai/ask")
                        .with(loggedInUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Kui palju maksab laenutus?\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("AI_UNSUPPORTED_QUESTION"))
                .andExpect(jsonPath("$.message").value(
                        "Saan vastata ainult saadaval tööriistade otsimise küsimustele."));
    }

    @Test
    void invalidGeneratedSqlReturnsOnlyGeneric500ApiError() throws Exception {
        String question = "Millised tööriistad on saadaval?";
        when(toolAskService.ask(question)).thenThrow(new InvalidToolSearchSqlException());

        mockMvc.perform(post("/api/ai/ask")
                        .with(loggedInUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Millised tööriistad on saadaval?\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value(
                        "Toiming ebaõnnestus. Palun proovi hiljem uuesti."))
                .andExpect(jsonPath("$.message").value(Matchers.not(Matchers.containsString("SQL"))));
    }

    private static RequestPostProcessor loggedInUser() {
        OidcIdToken token = OidcIdToken.withTokenValue("test-token")
                .subject("test-user")
                .build();
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_customer"));
        var principal = new AppUserPrincipal(1, authorities, token, null);
        var authToken = new OAuth2AuthenticationToken(principal, authorities, "google");
        return authentication(authToken);
    }
}
