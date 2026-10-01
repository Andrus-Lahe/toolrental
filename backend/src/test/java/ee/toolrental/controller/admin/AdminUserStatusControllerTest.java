package ee.toolrental.controller.admin;

import ee.toolrental.controller.admin.dto.UserStatusRequestDto;
import ee.toolrental.infrastructure.exception.ForbiddenException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.toolrental.infrastructure.security.AppUserOidcService;
import ee.toolrental.infrastructure.security.AppUserPrincipal;
import ee.toolrental.infrastructure.security.SecurityConfig;
import ee.toolrental.service.AdminUserStatusService;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserStatusController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "spring.security.oauth2.client.registration.google.client-id=test-client-id",
        "spring.security.oauth2.client.registration.google.client-secret=test-client-secret"})
class AdminUserStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminUserStatusService adminUserStatusService;

    @MockitoBean
    private AppUserOidcService appUserOidcService;

    /**
     * Admin blokeerib kasutaja: vastus on 200 tühja body'ga ning service saab admini ID (1), kasutaja ID (3) ja oleku B.
     */
    @Test
    void admin_blocksUser_returnsOkWithEmptyBody() throws Exception {
        mockMvc.perform(patchStatus(3, "{\"status\":\"B\"}").with(loggedInUser(1, "admin")))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(adminUserStatusService).changeUserStatus(1, 3, new UserStatusRequestDto("B"));
    }

    /**
     * Admin taastab kasutaja oleku väärtusega A ja vastus on samuti 200.
     */
    @Test
    void admin_activatesUser_returnsOk() throws Exception {
        mockMvc.perform(patchStatus(3, "{\"status\":\"A\"}").with(loggedInUser(1, "admin")))
                .andExpect(status().isOk());

        verify(adminUserStatusService).changeUserStatus(1, 3, new UserStatusRequestDto("A"));
    }

    /**
     * Sisse logimata kasutaja saab 401 tühja body'ga ja service'it ei kutsuta.
     */
    @Test
    void guest_getsUnauthorizedWithEmptyBody() throws Exception {
        mockMvc.perform(patchStatus(3, "{\"status\":\"B\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));

        verifyNoInteractions(adminUserStatusService);
    }

    /**
     * Customer-rolliga kasutaja saab 403 tühja body'ga ja service'it ei kutsuta.
     */
    @Test
    void customer_getsForbiddenWithEmptyBody() throws Exception {
        mockMvc.perform(patchStatus(3, "{\"status\":\"B\"}").with(loggedInUser(3, "customer")))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));

        verifyNoInteractions(adminUserStatusService);
    }

    /**
     * Puuduv status annab 400 INCORRECT_INPUT ja teate "status: ei tohi olla tühi"; service'it ei kutsuta.
     */
    @Test
    void missingStatus_producesBadRequest() throws Exception {
        mockMvc.perform(patchStatus(3, "{}").with(loggedInUser(1, "admin")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INCORRECT_INPUT"))
                .andExpect(jsonPath("$.message").value("status: ei tohi olla tühi"));

        verifyNoInteractions(adminUserStatusService);
    }

    /**
     * Muu väärtus kui A või B annab 400 INCORRECT_INPUT ja teate "status: peab olema A või B".
     */
    @Test
    void invalidStatus_producesBadRequest() throws Exception {
        mockMvc.perform(patchStatus(3, "{\"status\":\"X\"}").with(loggedInUser(1, "admin")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INCORRECT_INPUT"))
                .andExpect(jsonPath("$.message").value("status: peab olema A või B"));

        verifyNoInteractions(adminUserStatusService);
    }

    /**
     * Tekstiline userId annab 400 INCORRECT_INPUT ja service'it ei kutsuta.
     */
    @Test
    void nonIntegerUserId_producesBadRequest() throws Exception {
        mockMvc.perform(patch("/api/admin/users/abc/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"B\"}").with(loggedInUser(1, "admin")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INCORRECT_INPUT"));

        verifyNoInteractions(adminUserStatusService);
    }

    /**
     * Olematu kasutaja annab 404 PRIMARY_KEY_NOT_FOUND ja teate tegeliku ID-ga.
     */
    @Test
    void missingUser_producesNotFound() throws Exception {
        doThrow(new PrimaryKeyNotFoundException("userId", 123)).when(adminUserStatusService)
                .changeUserStatus(1, 123, new UserStatusRequestDto("B"));

        mockMvc.perform(patchStatus(123, "{\"status\":\"B\"}").with(loggedInUser(1, "admin")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRIMARY_KEY_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Ei leidnud primary keyd 'userId' väärtusega: 123"));
    }

    /**
     * Enda blokeerimine annab 403 SELF_BLOCK_NOT_ALLOWED ApiError kujul.
     */
    @Test
    void blockingSelf_producesForbiddenWithErrorCode() throws Exception {
        doThrow(new ForbiddenException("Iseennast ei saa blokeerida", "SELF_BLOCK_NOT_ALLOWED"))
                .when(adminUserStatusService).changeUserStatus(1, 1, new UserStatusRequestDto("B"));

        mockMvc.perform(patchStatus(1, "{\"status\":\"B\"}").with(loggedInUser(1, "admin")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("SELF_BLOCK_NOT_ALLOWED"))
                .andExpect(jsonPath("$.message").value("Iseennast ei saa blokeerida"));
    }

    /**
     * Andmebaasi tõrge annab 500 INTERNAL_SERVER_ERROR kokkulepitud sõnumiga.
     */
    @Test
    void changingFailure_producesInternalServerError() throws Exception {
        doThrow(new InternalServerErrorException("Kasutaja oleku muutmine ebaõnnestus. Palun proovi hiljem uuesti."))
                .when(adminUserStatusService).changeUserStatus(1, 3, new UserStatusRequestDto("B"));

        mockMvc.perform(patchStatus(3, "{\"status\":\"B\"}").with(loggedInUser(1, "admin")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("Kasutaja oleku muutmine ebaõnnestus. Palun proovi hiljem uuesti."));
    }

    /**
     * Koostab PATCH /api/admin/users/{userId}/status päringu antud JSON body'ga.
     * Sisselogimine lisatakse testis eraldi.
     */
    private static MockHttpServletRequestBuilder patchStatus(Integer userId, String json) {
        return patch("/api/admin/users/" + userId + "/status").contentType(MediaType.APPLICATION_JSON).content(json);
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
