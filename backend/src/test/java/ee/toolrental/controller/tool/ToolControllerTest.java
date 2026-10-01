package ee.toolrental.controller.tool;

import ee.toolrental.infrastructure.security.AppUserOidcService;
import ee.toolrental.infrastructure.security.AppUserPrincipal;
import ee.toolrental.infrastructure.security.SecurityConfig;
import ee.toolrental.service.ToolService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ToolController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "spring.security.oauth2.client.registration.google.client-id=test-client-id",
        "spring.security.oauth2.client.registration.google.client-secret=test-client-secret"})
class ToolControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean ToolService toolService;
    @MockitoBean AppUserOidcService appUserOidcService;

    @Test
    void customerCanCreateAndUserIdComesFromSession() throws Exception {
        mockMvc.perform(postTool(validJson()).with(loggedInUser(3, "customer")))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(toolService).createTool(eq(3), argThat(request -> request.getCategoryId() == 2
                && request.getName().equals("Akutrell") && request.getImageData().equals("")));
    }

    @Test
    void anonymousAndNonCustomerAreRejected() throws Exception {
        mockMvc.perform(postTool(validJson())).andExpect(status().isUnauthorized());
        mockMvc.perform(postTool(validJson()).with(loggedInUser(3, "admin")))
                .andExpect(status().isForbidden());
        verifyNoInteractions(toolService);
    }

    @Test
    void requestValidationReturns400() throws Exception {
        mockMvc.perform(postTool("{}").with(loggedInUser(3, "customer")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INCORRECT_INPUT"));
        mockMvc.perform(postTool(validJson().replace("Akutrell", " ")).with(loggedInUser(3, "customer")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("name: ei tohi olla tühi"));
        verifyNoInteractions(toolService);
    }

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder postTool(String json) {
        return post("/api/tools").contentType(MediaType.APPLICATION_JSON).content(json);
    }

    private static String validJson() {
        return "{\"ownerId\":999,\"categoryId\":2,\"name\":\"Akutrell\",\"description\":null,\"imageData\":\"\"}";
    }

    private static RequestPostProcessor loggedInUser(int userId, String role) {
        OidcIdToken token = OidcIdToken.withTokenValue("test-token").subject("sub-" + userId)
                .claim("email", "user@example.com").build();
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
        AppUserPrincipal principal = new AppUserPrincipal(userId, authorities, token, null);
        return authentication(new OAuth2AuthenticationToken(principal, authorities, "google"));
    }
}
