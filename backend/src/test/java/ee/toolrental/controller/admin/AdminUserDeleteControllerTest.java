package ee.toolrental.controller.admin;

import ee.toolrental.infrastructure.exception.ForbiddenException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.toolrental.infrastructure.security.AppUserOidcService;
import ee.toolrental.infrastructure.security.AppUserPrincipal;
import ee.toolrental.infrastructure.security.SecurityConfig;
import ee.toolrental.service.AdminUserDeleteService;
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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserDeleteController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "spring.security.oauth2.client.registration.google.client-id=test-client-id",
        "spring.security.oauth2.client.registration.google.client-secret=test-client-secret"})
class AdminUserDeleteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminUserDeleteService adminUserDeleteService;

    @MockitoBean
    private AppUserOidcService appUserOidcService;

    /**
     * Admin kustutab kasutaja: vastus on 200 tühja body'ga.
     * Service saab admini ID sessioonist (1) ja kustutatava ID path'ist (9).
     */
    @Test
    void admin_deletesUser_returnsOkWithEmptyBody() throws Exception {
        mockMvc.perform(delete("/api/admin/users/9").with(loggedInUser(1, "admin")))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(adminUserDeleteService).deleteUser(1, 9);
    }

    /**
     * Sisse logimata kasutaja saab 401 tühja body'ga ja service'it ei kutsuta.
     */
    @Test
    void guest_getsUnauthorizedWithEmptyBody() throws Exception {
        mockMvc.perform(delete("/api/admin/users/9"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));

        verifyNoInteractions(adminUserDeleteService);
    }

    /**
     * Customer-rolliga kasutaja saab 403 tühja body'ga ja service'it ei kutsuta.
     */
    @Test
    void customer_getsForbiddenWithEmptyBody() throws Exception {
        mockMvc.perform(delete("/api/admin/users/9").with(loggedInUser(3, "customer")))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));

        verifyNoInteractions(adminUserDeleteService);
    }

    /**
     * Olematu kasutaja annab 404 PRIMARY_KEY_NOT_FOUND ja teate tegeliku ID-ga.
     */
    @Test
    void missingUser_producesNotFound() throws Exception {
        doThrow(new PrimaryKeyNotFoundException("userId", 123)).when(adminUserDeleteService).deleteUser(1, 123);

        mockMvc.perform(delete("/api/admin/users/123").with(loggedInUser(1, "admin")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRIMARY_KEY_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Ei leidnud primary keyd 'userId' väärtusega: 123"));
    }

    /**
     * Admini enda kustutamine annab 403 SELF_DELETE_NOT_ALLOWED ApiError kujul.
     */
    @Test
    void deletingSelf_producesForbiddenWithErrorCode() throws Exception {
        doThrow(new ForbiddenException("Iseennast ei saa kustutada", "SELF_DELETE_NOT_ALLOWED"))
                .when(adminUserDeleteService).deleteUser(1, 1);

        mockMvc.perform(delete("/api/admin/users/1").with(loggedInUser(1, "admin")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("SELF_DELETE_NOT_ALLOWED"))
                .andExpect(jsonPath("$.message").value("Iseennast ei saa kustutada"));
    }

    /**
     * Kasutaja, kellel on tööriistu või broneeringuid, annab 403 USER_HAS_DATA.
     */
    @Test
    void userWithData_producesForbiddenWithErrorCode() throws Exception {
        doThrow(new ForbiddenException("Kasutajat ei saa kustutada, sest tal on tööriistu või broneeringuid", "USER_HAS_DATA"))
                .when(adminUserDeleteService).deleteUser(1, 3);

        mockMvc.perform(delete("/api/admin/users/3").with(loggedInUser(1, "admin")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("USER_HAS_DATA"))
                .andExpect(jsonPath("$.message").value("Kasutajat ei saa kustutada, sest tal on tööriistu või broneeringuid"));
    }

    /**
     * Andmebaasi tõrge annab 500 INTERNAL_SERVER_ERROR kokkulepitud sõnumiga.
     */
    @Test
    void deletingFailure_producesInternalServerError() throws Exception {
        doThrow(new InternalServerErrorException("Kasutaja kustutamine ebaõnnestus. Palun proovi hiljem uuesti."))
                .when(adminUserDeleteService).deleteUser(1, 9);

        mockMvc.perform(delete("/api/admin/users/9").with(loggedInUser(1, "admin")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("Kasutaja kustutamine ebaõnnestus. Palun proovi hiljem uuesti."));
    }

    /**
     * Täisarvuline userId on nõutav: tekstiline väärtus annab 400 INCORRECT_INPUT ja service'it ei kutsuta.
     */
    @Test
    void nonIntegerUserId_producesBadRequest() throws Exception {
        mockMvc.perform(delete("/api/admin/users/abc").with(loggedInUser(1, "admin")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INCORRECT_INPUT"));

        verifyNoInteractions(adminUserDeleteService);
    }

    /**
     * Loob testis sisselogitud kasutaja antud ID ja rolliga (admin või customer).
     * Principal sisaldab rakenduse kasutaja ID-d ja Google'i e-posti.
     */
    private static RequestPostProcessor loggedInUser(Integer userId, String roleName) {
        OidcIdToken oidcIdToken = OidcIdToken.withTokenValue("test-token")
                .subject("google-sub-" + userId)
                .claim("email", "user@gmail.com")
                .build();
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + roleName));
        AppUserPrincipal appUserPrincipal = new AppUserPrincipal(userId, authorities, oidcIdToken, null);
        return authentication(new OAuth2AuthenticationToken(appUserPrincipal, authorities, "google"));
    }
}
