package ee.toolrental.controller.admin;

import ee.toolrental.controller.admin.dto.AdminCategoryDto;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.infrastructure.security.AppUserOidcService;
import ee.toolrental.infrastructure.security.AppUserPrincipal;
import ee.toolrental.infrastructure.security.SecurityConfig;
import ee.toolrental.service.AdminCategoryListService;
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

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminCategoryListController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "spring.security.oauth2.client.registration.google.client-id=test-client-id",
        "spring.security.oauth2.client.registration.google.client-secret=test-client-secret"})
class AdminCategoryListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminCategoryListService adminCategoryListService;

    @MockitoBean
    private AppUserOidcService appUserOidcService;

    /**
     * Admin saab GET päringuga kategooriate massiivi, kus igal real on täpselt neli kokkulepitud välja.
     * Kirjeldus null tagastatakse väärtusega null (väli on olemas).
     */
    @Test
    void admin_getsCategoriesWithExpectedShape() throws Exception {
        when(adminCategoryListService.getCategories()).thenReturn(List.of(
                new AdminCategoryDto(1, "Aiatööd", "Muruniidukid, labidad, rehad", 100),
                new AdminCategoryDto(5, "Uus", null, 500)));

        mockMvc.perform(get("/api/admin/categories").with(loggedInUser("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].length()").value(4))
                .andExpect(jsonPath("$[0].categoryId").value(1))
                .andExpect(jsonPath("$[0].categoryName").value("Aiatööd"))
                .andExpect(jsonPath("$[0].description").value("Muruniidukid, labidad, rehad"))
                .andExpect(jsonPath("$[0].sequence").value(100))
                .andExpect(jsonPath("$[1].length()").value(4))
                .andExpect(jsonPath("$[1].description").isEmpty())
                .andExpect(jsonPath("$[1].sequence").value(500));
    }

    /**
     * Kategooriaid pole: vastus on 200 ja tühi massiiv.
     */
    @Test
    void admin_noCategories_returnsEmptyArray() throws Exception {
        when(adminCategoryListService.getCategories()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/categories").with(loggedInUser("admin")))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    /**
     * Sisse logimata kasutaja saab 401 ilma body'ta ja service'it ei kutsuta.
     */
    @Test
    void guest_getsUnauthorizedWithEmptyBody() throws Exception {
        mockMvc.perform(get("/api/admin/categories"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));

        verifyNoInteractions(adminCategoryListService);
    }

    /**
     * Customer-rolliga kasutaja saab 403 ilma body'ta ja service'it ei kutsuta.
     */
    @Test
    void customer_getsForbiddenWithEmptyBody() throws Exception {
        mockMvc.perform(get("/api/admin/categories").with(loggedInUser("customer")))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));

        verifyNoInteractions(adminCategoryListService);
    }

    /**
     * Andmebaasi tõrge annab 500 ja ApiError kuju errorCode + message, ilma tehniliste detailideta.
     */
    @Test
    void loadingFailure_producesSpecifiedApiError() throws Exception {
        when(adminCategoryListService.getCategories()).thenThrow(new InternalServerErrorException(
                "Kategooriate laadimine ebaõnnestus. Palun proovi hiljem uuesti."));

        mockMvc.perform(get("/api/admin/categories").with(loggedInUser("admin")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("Kategooriate laadimine ebaõnnestus. Palun proovi hiljem uuesti."));
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
