package ee.toolrental.controller.appuser;

import ee.toolrental.controller.appuser.dto.UserDetailResponse;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.toolrental.infrastructure.security.AppUserOidcService;
import ee.toolrental.infrastructure.security.AppUserPrincipal;
import ee.toolrental.infrastructure.security.SecurityConfig;
import ee.toolrental.service.UserDetailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserDetailController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "spring.security.oauth2.client.registration.google.client-id=test-client-id",
        "spring.security.oauth2.client.registration.google.client-secret=test-client-secret"})
class UserDetailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserDetailService userDetailService;

    @MockitoBean
    private AppUserOidcService appUserOidcService;

    @Test
    void getUserDetails_authenticatedUser_returnsExactlyFiveFields() throws Exception {
        when(userDetailService.getUserDetails(1)).thenReturn(response(1, "Marko", "Tamm", "email@Gmail.com", "56565656"));

        mockMvc.perform(get("/api/users/1").with(loggedInUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.firstName").value("Marko"))
                .andExpect(jsonPath("$.lastName").value("Tamm"))
                .andExpect(jsonPath("$.email").value("email@Gmail.com"))
                .andExpect(jsonPath("$.phone").value("56565656"));
        verify(userDetailService).getUserDetails(1);
    }

    @Test
    void getUserDetails_notAuthenticated_returnsEmpty401() throws Exception {
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));
        verifyNoInteractions(userDetailService);
    }

    @Test
    void getUserDetails_invalidId_returns400() throws Exception {
        mockMvc.perform(get("/api/users/not-a-number").with(loggedInUser()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INCORRECT_INPUT"))
                .andExpect(jsonPath("$.message").value("userId: peab olema Integer-tüüpi täisarv"));
        verifyNoInteractions(userDetailService);
    }

    @Test
    void getUserDetails_unknownUser_returns404() throws Exception {
        when(userDetailService.getUserDetails(123)).thenThrow(new PrimaryKeyNotFoundException("userId", 123));

        mockMvc.perform(get("/api/users/123").with(loggedInUser()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRIMARY_KEY_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Ei leidnud primary keyd 'userId' väärtusega: 123"));
    }

    @Test
    void getUserDetails_databaseFailure_returns500() throws Exception {
        when(userDetailService.getUserDetails(1)).thenThrow(new InternalServerErrorException(
                "Kasutaja andmete laadimine ebaõnnestus. Palun proovi hiljem uuesti."));

        mockMvc.perform(get("/api/users/1").with(loggedInUser()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("Kasutaja andmete laadimine ebaõnnestus. Palun proovi hiljem uuesti."));
    }

    private UserDetailResponse response(Integer userId, String firstName, String lastName, String email, String phone) {
        UserDetailResponse response = new UserDetailResponse();
        response.setUserId(userId);
        response.setFirstName(firstName);
        response.setLastName(lastName);
        response.setEmail(email);
        response.setPhone(phone);
        return response;
    }

    private static RequestPostProcessor loggedInUser() {
        OidcIdToken oidcIdToken = OidcIdToken.withTokenValue("test-token")
                .subject("google-sub-10")
                .claim("email", "renter@example.com")
                .build();
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_customer"));
        AppUserPrincipal principal = new AppUserPrincipal(10, authorities, oidcIdToken, null);
        return authentication(new OAuth2AuthenticationToken(principal, authorities, "google"));
    }
}
