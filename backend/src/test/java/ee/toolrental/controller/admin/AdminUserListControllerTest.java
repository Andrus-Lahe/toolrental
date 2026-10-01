package ee.toolrental.controller.admin;

import ee.toolrental.controller.admin.dto.AdminUserDto;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.infrastructure.security.AppUserOidcService;
import ee.toolrental.infrastructure.security.AppUserPrincipal;
import ee.toolrental.infrastructure.security.SecurityConfig;
import ee.toolrental.service.AdminUserListService;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserListController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "spring.security.oauth2.client.registration.google.client-id=test-client-id",
        "spring.security.oauth2.client.registration.google.client-secret=test-client-secret"})
class AdminUserListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminUserListService adminUserListService;

    @MockitoBean
    private AppUserOidcService appUserOidcService;

    /**
     * Admin saab GET päringuga kasutajate massiivi, kus igal real on täpselt seitse kokkulepitud välja.
     * Profiilita kasutajal on email ja registeredAt null ning kuupäev on kujul yyyy-MM-dd.
     */
    @Test
    void admin_getsUsersWithExpectedShape() throws Exception {
        when(adminUserListService.getUsers()).thenReturn(List.of(
                new AdminUserDto(1, "Marko", "Tamm", "email@Gmail.com", "admin", LocalDate.of(2026, 9, 18), "A"),
                new AdminUserDto(7, "Mari", "", null, "customer", null, "B")));

        mockMvc.perform(get("/api/admin/users").with(loggedInUser("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].length()").value(7))
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].firstName").value("Marko"))
                .andExpect(jsonPath("$[0].lastName").value("Tamm"))
                .andExpect(jsonPath("$[0].email").value("email@Gmail.com"))
                .andExpect(jsonPath("$[0].roleName").value("admin"))
                .andExpect(jsonPath("$[0].registeredAt").value("2026-09-18"))
                .andExpect(jsonPath("$[0].status").value("A"))
                .andExpect(jsonPath("$[1].userId").value(7))
                .andExpect(jsonPath("$[1].email").doesNotExist())
                .andExpect(jsonPath("$[1].registeredAt").doesNotExist())
                .andExpect(jsonPath("$[1].status").value("B"))
                .andExpect(jsonPath("$[0].googleSub").doesNotExist());
    }

    /**
     * Kasutajaid veel ei ole: vastus on 200 ja tühi massiiv.
     */
    @Test
    void admin_noUsers_returnsEmptyArray() throws Exception {
        when(adminUserListService.getUsers()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/users").with(loggedInUser("admin")))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    /**
     * Sisse logimata kasutaja saab 401 ilma body'ta ja service'it ei kutsuta.
     */
    @Test
    void guest_getsUnauthorizedWithEmptyBody() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));

        verifyNoInteractions(adminUserListService);
    }

    /**
     * Customer-rolliga kasutaja saab 403 ilma body'ta ja service'it ei kutsuta.
     */
    @Test
    void customer_getsForbiddenWithEmptyBody() throws Exception {
        mockMvc.perform(get("/api/admin/users").with(loggedInUser("customer")))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));

        verifyNoInteractions(adminUserListService);
    }

    /**
     * Andmebaasi tõrge annab 500 ja ApiError kuju errorCode + message, ilma tehniliste detailideta.
     */
    @Test
    void loadingFailure_producesSpecifiedApiError() throws Exception {
        when(adminUserListService.getUsers()).thenThrow(new InternalServerErrorException(
                "Kasutajate laadimine ebaõnnestus. Palun proovi hiljem uuesti."));

        mockMvc.perform(get("/api/admin/users").with(loggedInUser("admin")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("Kasutajate laadimine ebaõnnestus. Palun proovi hiljem uuesti."));
    }

    /**
     * Loob testis sisselogitud kasutaja antud rolliga (admin või customer).
     * Principal sisaldab rakenduse kasutaja ID-d 1 ja Google'i e-posti.
     */
    private static RequestPostProcessor loggedInUser(String roleName) {
        OidcIdToken oidcIdToken = OidcIdToken.withTokenValue("test-token")
                .subject("google-sub-1")
                .claim("email", "user@gmail.com")
                .build();
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + roleName));
        AppUserPrincipal appUserPrincipal = new AppUserPrincipal(1, authorities, oidcIdToken, null);
        return authentication(new OAuth2AuthenticationToken(appUserPrincipal, authorities, "google"));
    }
}
