package ee.toolrental.controller.admin;

import ee.toolrental.controller.admin.dto.CategoryCreateRequestDto;
import ee.toolrental.infrastructure.exception.ForbiddenException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.infrastructure.security.AppUserOidcService;
import ee.toolrental.infrastructure.security.AppUserPrincipal;
import ee.toolrental.infrastructure.security.SecurityConfig;
import ee.toolrental.service.AdminCategoryCreateService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminCategoryCreateController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "spring.security.oauth2.client.registration.google.client-id=test-client-id",
        "spring.security.oauth2.client.registration.google.client-secret=test-client-secret"})
class AdminCategoryCreateControllerTest {

    private static final String VALID_JSON = """
            {"categoryName": "Talvetööd", "description": "Lumelabidad, jääpurustajad", "sequence": 400}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminCategoryCreateService adminCategoryCreateService;

    @MockitoBean
    private AppUserOidcService appUserOidcService;

    /**
     * Admin lisab kategooria: vastus on 200 tühja body'ga ja service saab päringu väärtused.
     */
    @Test
    void admin_createsCategory_returnsOkWithEmptyBody() throws Exception {
        mockMvc.perform(postCategory(VALID_JSON).with(loggedInUser("admin")))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(adminCategoryCreateService).createCategory(
                new CategoryCreateRequestDto("Talvetööd", "Lumelabidad, jääpurustajad", 400));
    }

    /**
     * Kirjeldus võib puududa (null): päring on endiselt 200.
     */
    @Test
    void admin_withoutDescription_returnsOk() throws Exception {
        mockMvc.perform(postCategory("{\"categoryName\":\"Talvetööd\",\"sequence\":400}").with(loggedInUser("admin")))
                .andExpect(status().isOk());

        verify(adminCategoryCreateService).createCategory(new CategoryCreateRequestDto("Talvetööd", null, 400));
    }

    /**
     * Sisse logimata kasutaja saab 401 tühja body'ga ja service'it ei kutsuta.
     */
    @Test
    void guest_getsUnauthorizedWithEmptyBody() throws Exception {
        mockMvc.perform(postCategory(VALID_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));

        verifyNoInteractions(adminCategoryCreateService);
    }

    /**
     * Customer-rolliga kasutaja saab 403 tühja body'ga ja service'it ei kutsuta.
     */
    @Test
    void customer_getsForbiddenWithEmptyBody() throws Exception {
        mockMvc.perform(postCategory(VALID_JSON).with(loggedInUser("customer")))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));

        verifyNoInteractions(adminCategoryCreateService);
    }

    /**
     * Tühi või puuduv categoryName annab 400 INCORRECT_INPUT ja teate "categoryName: ei tohi olla tühi".
     */
    @Test
    void blankCategoryName_producesBadRequest() throws Exception {
        mockMvc.perform(postCategory("{\"categoryName\":\"  \",\"sequence\":400}").with(loggedInUser("admin")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INCORRECT_INPUT"))
                .andExpect(jsonPath("$.message").value("categoryName: ei tohi olla tühi"));

        verifyNoInteractions(adminCategoryCreateService);
    }

    /**
     * Puuduv sequence annab 400 INCORRECT_INPUT ja teate "sequence: ei tohi olla tühi".
     */
    @Test
    void missingSequence_producesBadRequest() throws Exception {
        mockMvc.perform(postCategory("{\"categoryName\":\"Talvetööd\"}").with(loggedInUser("admin")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INCORRECT_INPUT"))
                .andExpect(jsonPath("$.message").value("sequence: ei tohi olla tühi"));

        verifyNoInteractions(adminCategoryCreateService);
    }

    /**
     * Liiga pikk nimi (101 märki) annab 400 INCORRECT_INPUT; täpselt 100 märki on lubatud.
     */
    @Test
    void tooLongCategoryName_producesBadRequestButLimitIsAllowed() throws Exception {
        mockMvc.perform(postCategory("{\"categoryName\":\"" + "a".repeat(101) + "\",\"sequence\":1}").with(loggedInUser("admin")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("categoryName: ei tohi olla pikem kui 100 märki"));

        mockMvc.perform(postCategory("{\"categoryName\":\"" + "a".repeat(100) + "\",\"sequence\":1}").with(loggedInUser("admin")))
                .andExpect(status().isOk());
    }

    /**
     * Liiga pikk kirjeldus (256 märki) annab 400 INCORRECT_INPUT; täpselt 255 märki on lubatud.
     */
    @Test
    void tooLongDescription_producesBadRequestButLimitIsAllowed() throws Exception {
        mockMvc.perform(postCategory("{\"categoryName\":\"Uus\",\"description\":\"" + "a".repeat(256) + "\",\"sequence\":1}")
                        .with(loggedInUser("admin")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("description: ei tohi olla pikem kui 255 märki"));

        mockMvc.perform(postCategory("{\"categoryName\":\"Uus\",\"description\":\"" + "a".repeat(255) + "\",\"sequence\":1}")
                        .with(loggedInUser("admin")))
                .andExpect(status().isOk());
    }

    /**
     * Olemasolev nimi annab 403 CATEGORY_UNAVAILABLE ApiError kujul.
     */
    @Test
    void existingName_producesForbiddenWithErrorCode() throws Exception {
        doThrow(new ForbiddenException("Sellise nimega kategooria on juba olemas", "CATEGORY_UNAVAILABLE"))
                .when(adminCategoryCreateService).createCategory(new CategoryCreateRequestDto("Aiatööd", null, 100));

        mockMvc.perform(postCategory("{\"categoryName\":\"Aiatööd\",\"sequence\":100}").with(loggedInUser("admin")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("CATEGORY_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value("Sellise nimega kategooria on juba olemas"));
    }

    /**
     * Andmebaasi tõrge annab 500 INTERNAL_SERVER_ERROR kokkulepitud sõnumiga.
     */
    @Test
    void creatingFailure_producesInternalServerError() throws Exception {
        doThrow(new InternalServerErrorException("Kategooria lisamine ebaõnnestus. Palun proovi hiljem uuesti."))
                .when(adminCategoryCreateService).createCategory(new CategoryCreateRequestDto("Talvetööd", null, 400));

        mockMvc.perform(postCategory("{\"categoryName\":\"Talvetööd\",\"sequence\":400}").with(loggedInUser("admin")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("Kategooria lisamine ebaõnnestus. Palun proovi hiljem uuesti."));
    }

    /**
     * Koostab POST /api/admin/categories päringu antud JSON body'ga.
     * Sisselogimine lisatakse testis eraldi.
     */
    private static MockHttpServletRequestBuilder postCategory(String json) {
        return post("/api/admin/categories").contentType(MediaType.APPLICATION_JSON).content(json);
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
