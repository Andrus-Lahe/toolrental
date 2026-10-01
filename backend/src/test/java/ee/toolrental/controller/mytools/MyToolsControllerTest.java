package ee.toolrental.controller.mytools;

import ee.toolrental.controller.mytools.dto.MyToolsResponseDto;
import ee.toolrental.infrastructure.security.AppUserOidcService;
import ee.toolrental.infrastructure.security.AppUserPrincipal;
import ee.toolrental.infrastructure.security.SecurityConfig;
import ee.toolrental.service.MyToolsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MyToolsController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "spring.security.oauth2.client.registration.google.client-id=test-client-id",
        "spring.security.oauth2.client.registration.google.client-secret=test-client-secret"})
class MyToolsControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean MyToolsService myToolsService;
    @MockitoBean AppUserOidcService appUserOidcService;

    @Test
    void customerGetsOnlyTheirSessionData() throws Exception {
        MyToolsResponseDto response = new MyToolsResponseDto();
        response.setUserId(3);
        response.setFirstName("Liis");
        response.setLastName("Kask");
        response.setEmail("liis.kask@example.com");
        response.setPhone("55501002");
        when(myToolsService.getMyTools(3)).thenReturn(response);

        mockMvc.perform(get("/api/users/me/tools").with(loggedInUser(3, "customer")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(3))
                .andExpect(jsonPath("$.firstName").value("Liis"))
                .andExpect(jsonPath("$.email").value("liis.kask@example.com"))
                .andExpect(jsonPath("$.myRentals").isArray())
                .andExpect(jsonPath("$.availableTools").isArray());
        verify(myToolsService).getMyTools(3);
    }

    @Test
    void adminMayReadTheirOwnOverview() throws Exception {
        MyToolsResponseDto response = new MyToolsResponseDto();
        response.setUserId(1);
        when(myToolsService.getMyTools(1)).thenReturn(response);

        mockMvc.perform(get("/api/users/me/tools").with(loggedInUser(1, "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));
        verify(myToolsService).getMyTools(1);
    }

    @Test
    void anonymousReceivesEndpointSpecific401Json() throws Exception {
        mockMvc.perform(get("/api/users/me/tools"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTHENTICATION_REQUIRED"))
                .andExpect(jsonPath("$.message").value("Vaate avamiseks logi sisse."));
        verifyNoInteractions(myToolsService);
    }

    private static RequestPostProcessor loggedInUser(int userId, String role) {
        OidcIdToken token = OidcIdToken.withTokenValue("test-token").subject("sub-" + userId)
                .claim("email", "user@example.com").build();
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
        AppUserPrincipal principal = new AppUserPrincipal(userId, authorities, token, null);
        return authentication(new OAuth2AuthenticationToken(principal, authorities, "google"));
    }
}
