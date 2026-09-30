package ee.toolrental.controller.ai;

import ee.toolrental.infrastructure.security.AppUserOidcService;
import ee.toolrental.infrastructure.security.AppUserPrincipal;
import ee.toolrental.infrastructure.security.SecurityConfig;
import ee.toolrental.persistence.ai.AvailableTool;
import ee.toolrental.service.ToolAskService;
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

import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ToolAskController.class)
@Import(SecurityConfig.class)
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
                "Kristiines on saadaval Akutrell.",
                List.of(new AvailableTool(1, "Akutrell", "Puurid", "Tallinn", "Kristiine"))));

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
                .andExpect(jsonPath("$.answer").value("Kristiines on saadaval Akutrell."))
                .andExpect(jsonPath("$.tools[0].name").value("Akutrell"))
                .andExpect(jsonPath("$.tools[0].district").value("Kristiine"))
                .andExpect(jsonPath("$.tools[0].email").doesNotExist());
    }
}
