package ee.toolrental.controller.admin;

import ee.toolrental.infrastructure.exception.ForbiddenException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.toolrental.infrastructure.security.AppUserOidcService;
import ee.toolrental.infrastructure.security.AppUserPrincipal;
import ee.toolrental.infrastructure.security.SecurityConfig;
import ee.toolrental.service.AdminCategoryDeleteService;
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

@WebMvcTest(AdminCategoryDeleteController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "spring.security.oauth2.client.registration.google.client-id=test-client-id",
        "spring.security.oauth2.client.registration.google.client-secret=test-client-secret"})
class AdminCategoryDeleteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminCategoryDeleteService adminCategoryDeleteService;

    @MockitoBean
    private AppUserOidcService appUserOidcService;

    /**
     * Admin kustutab kategooria: vastus on 200 tühja body'ga ja service saab path'i ID (9).
     */
    @Test
    void admin_deletesCategory_returnsOkWithEmptyBody() throws Exception {
        mockMvc.perform(delete("/api/admin/categories/9").with(loggedInUser("admin")))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(adminCategoryDeleteService).deleteCategory(9);
    }

    /**
     * Sisse logimata kasutaja saab 401 tühja body'ga ja service'it ei kutsuta.
     */
    @Test
    void guest_getsUnauthorizedWithEmptyBody() throws Exception {
        mockMvc.perform(delete("/api/admin/categories/9"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));

        verifyNoInteractions(adminCategoryDeleteService);
    }

    /**
     * Customer-rolliga kasutaja saab 403 tühja body'ga ja service'it ei kutsuta.
     */
    @Test
    void customer_getsForbiddenWithEmptyBody() throws Exception {
        mockMvc.perform(delete("/api/admin/categories/9").with(loggedInUser("customer")))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));

        verifyNoInteractions(adminCategoryDeleteService);
    }

    /**
     * Olematu kategooria annab 404 PRIMARY_KEY_NOT_FOUND ja teate tegeliku ID-ga.
     */
    @Test
    void missingCategory_producesNotFound() throws Exception {
        doThrow(new PrimaryKeyNotFoundException("categoryId", 123)).when(adminCategoryDeleteService).deleteCategory(123);

        mockMvc.perform(delete("/api/admin/categories/123").with(loggedInUser("admin")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRIMARY_KEY_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Ei leidnud primary keyd 'categoryId' väärtusega: 123"));
    }

    /**
     * Kategooria, milles on tööriistu, annab 403 CATEGORY_IN_USE ApiError kujul.
     */
    @Test
    void categoryWithTools_producesForbiddenWithErrorCode() throws Exception {
        doThrow(new ForbiddenException("Kategooriat ei saa kustutada, sest sellel on tööriistu", "CATEGORY_IN_USE"))
                .when(adminCategoryDeleteService).deleteCategory(4);

        mockMvc.perform(delete("/api/admin/categories/4").with(loggedInUser("admin")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("CATEGORY_IN_USE"))
                .andExpect(jsonPath("$.message").value("Kategooriat ei saa kustutada, sest sellel on tööriistu"));
    }

    /**
     * Andmebaasi tõrge annab 500 INTERNAL_SERVER_ERROR kokkulepitud sõnumiga.
     */
    @Test
    void deletingFailure_producesInternalServerError() throws Exception {
        doThrow(new InternalServerErrorException("Kategooria kustutamine ebaõnnestus. Palun proovi hiljem uuesti."))
                .when(adminCategoryDeleteService).deleteCategory(9);

        mockMvc.perform(delete("/api/admin/categories/9").with(loggedInUser("admin")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("Kategooria kustutamine ebaõnnestus. Palun proovi hiljem uuesti."));
    }

    /**
     * Tekstiline categoryId annab 400 INCORRECT_INPUT ja service'it ei kutsuta.
     */
    @Test
    void nonIntegerCategoryId_producesBadRequest() throws Exception {
        mockMvc.perform(delete("/api/admin/categories/abc").with(loggedInUser("admin")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INCORRECT_INPUT"));

        verifyNoInteractions(adminCategoryDeleteService);
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
