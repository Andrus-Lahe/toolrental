package ee.toolrental.controller.tool;

import ee.toolrental.infrastructure.exception.ForbiddenException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.toolrental.infrastructure.security.AppUserOidcService;
import ee.toolrental.infrastructure.security.AppUserPrincipal;
import ee.toolrental.infrastructure.security.SecurityConfig;
import ee.toolrental.service.ToolDeleteService;
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

@WebMvcTest(ToolDeleteController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "spring.security.oauth2.client.registration.google.client-id=test-client-id",
        "spring.security.oauth2.client.registration.google.client-secret=test-client-secret"})
class ToolDeleteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ToolDeleteService toolDeleteService;

    @MockitoBean
    private AppUserOidcService appUserOidcService;

    /**
     * Sisselogitud kasutaja kustutab tööriista: vastus on 200 tühja body'ga ja service saab sessiooni kasutaja ID (5) ning path'i ID (9).
     */
    @Test
    void loggedInUser_deletesTool_returnsOkWithEmptyBody() throws Exception {
        mockMvc.perform(delete("/api/tools/9").with(loggedInUser(5, "customer")))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(toolDeleteService).deleteTool(5, 9);
    }

    /**
     * Ka admin-rolliga kasutaja saab kustutada oma tööriista (reegel on omaniku, mitte rolli põhine).
     */
    @Test
    void admin_deletesOwnTool_returnsOk() throws Exception {
        mockMvc.perform(delete("/api/tools/9").with(loggedInUser(1, "admin")))
                .andExpect(status().isOk());

        verify(toolDeleteService).deleteTool(1, 9);
    }

    /**
     * Sisse logimata kasutaja saab 401 tühja body'ga ja service'it ei kutsuta.
     */
    @Test
    void guest_getsUnauthorizedWithEmptyBody() throws Exception {
        mockMvc.perform(delete("/api/tools/9"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));

        verifyNoInteractions(toolDeleteService);
    }

    /**
     * Olematu tööriist annab 404 PRIMARY_KEY_NOT_FOUND ja teate tegeliku ID-ga.
     */
    @Test
    void missingTool_producesNotFound() throws Exception {
        doThrow(new PrimaryKeyNotFoundException("toolId", 123)).when(toolDeleteService).deleteTool(5, 123);

        mockMvc.perform(delete("/api/tools/123").with(loggedInUser(5, "customer")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRIMARY_KEY_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Ei leidnud primary keyd 'toolId' väärtusega: 123"));
    }

    /**
     * Teise kasutaja tööriist annab 403 TOOL_NOT_OWNED ApiError kujul.
     */
    @Test
    void toolOfOtherUser_producesForbiddenWithErrorCode() throws Exception {
        doThrow(new ForbiddenException("Tööriista saab kustutada ainult selle omanik", "TOOL_NOT_OWNED"))
                .when(toolDeleteService).deleteTool(5, 4);

        mockMvc.perform(delete("/api/tools/4").with(loggedInUser(5, "customer")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("TOOL_NOT_OWNED"))
                .andExpect(jsonPath("$.message").value("Tööriista saab kustutada ainult selle omanik"));
    }

    /**
     * Tööriist broneeringutega annab 403 TOOL_HAS_BOOKINGS ApiError kujul.
     */
    @Test
    void toolWithBookings_producesForbiddenWithErrorCode() throws Exception {
        doThrow(new ForbiddenException("Tööriista ei saa kustutada, sest sellel on broneeringuid", "TOOL_HAS_BOOKINGS"))
                .when(toolDeleteService).deleteTool(1, 1);

        mockMvc.perform(delete("/api/tools/1").with(loggedInUser(1, "admin")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("TOOL_HAS_BOOKINGS"))
                .andExpect(jsonPath("$.message").value("Tööriista ei saa kustutada, sest sellel on broneeringuid"));
    }

    /**
     * Andmebaasi tõrge annab 500 INTERNAL_SERVER_ERROR kokkulepitud sõnumiga.
     */
    @Test
    void deletingFailure_producesInternalServerError() throws Exception {
        doThrow(new InternalServerErrorException("Tööriista kustutamine ebaõnnestus. Palun proovi hiljem uuesti."))
                .when(toolDeleteService).deleteTool(5, 9);

        mockMvc.perform(delete("/api/tools/9").with(loggedInUser(5, "customer")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("Tööriista kustutamine ebaõnnestus. Palun proovi hiljem uuesti."));
    }

    /**
     * Tekstiline toolId annab 400 INCORRECT_INPUT ja service'it ei kutsuta.
     */
    @Test
    void nonIntegerToolId_producesBadRequest() throws Exception {
        mockMvc.perform(delete("/api/tools/abc").with(loggedInUser(5, "customer")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INCORRECT_INPUT"));

        verifyNoInteractions(toolDeleteService);
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
