package ee.toolrental.controller.admin;

import ee.toolrental.controller.admin.dto.CategoryUpdateRequestDto;
import ee.toolrental.infrastructure.exception.ForbiddenException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.toolrental.infrastructure.security.AppUserOidcService;
import ee.toolrental.infrastructure.security.AppUserPrincipal;
import ee.toolrental.infrastructure.security.SecurityConfig;
import ee.toolrental.service.AdminCategoryUpdateService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminCategoryUpdateController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "spring.security.oauth2.client.registration.google.client-id=test-client-id",
        "spring.security.oauth2.client.registration.google.client-secret=test-client-secret"})
class AdminCategoryUpdateControllerTest {

    private static final String VALID_JSON = """
            {"categoryName": "Aiatööd", "description": "Muruniidukid, labidad, rehad", "sequence": 100}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminCategoryUpdateService adminCategoryUpdateService;

    @MockitoBean
    private AppUserOidcService appUserOidcService;

    /**
     * Admin muudab kategooria: vastus on 200 tühja body'ga ja service saab path'i ID (1) ning päringu väärtused.
     */
    @Test
    void admin_updatesCategory_returnsOkWithEmptyBody() throws Exception {
        mockMvc.perform(putCategory("1", VALID_JSON).with(loggedInUser("admin")))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(adminCategoryUpdateService).updateCategory(1,
                new CategoryUpdateRequestDto("Aiatööd", "Muruniidukid, labidad, rehad", 100));
    }

    /**
     * Kirjeldus võib puududa (null): päring on endiselt 200.
     */
    @Test
    void admin_withoutDescription_returnsOk() throws Exception {
        mockMvc.perform(putCategory("1", "{\"categoryName\":\"Aiatööd\",\"sequence\":100}").with(loggedInUser("admin")))
                .andExpect(status().isOk());

        verify(adminCategoryUpdateService).updateCategory(1, new CategoryUpdateRequestDto("Aiatööd", null, 100));
    }

    /**
     * Sisse logimata kasutaja saab 401 tühja body'ga ja service'it ei kutsuta.
     */
    @Test
    void guest_getsUnauthorizedWithEmptyBody() throws Exception {
        mockMvc.perform(putCategory("1", VALID_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));

        verifyNoInteractions(adminCategoryUpdateService);
    }

    /**
     * Customer-rolliga kasutaja saab 403 tühja body'ga ja service'it ei kutsuta.
     */
    @Test
    void customer_getsForbiddenWithEmptyBody() throws Exception {
        mockMvc.perform(putCategory("1", VALID_JSON).with(loggedInUser("customer")))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));

        verifyNoInteractions(adminCategoryUpdateService);
    }

    /**
     * Tühi categoryName annab 400 INCORRECT_INPUT ja teate "categoryName: ei tohi olla tühi".
     */
    @Test
    void blankCategoryName_producesBadRequest() throws Exception {
        mockMvc.perform(putCategory("1", "{\"categoryName\":\"\",\"sequence\":100}").with(loggedInUser("admin")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INCORRECT_INPUT"))
                .andExpect(jsonPath("$.message").value("categoryName: ei tohi olla tühi"));

        verifyNoInteractions(adminCategoryUpdateService);
    }

    /**
     * Puuduv sequence annab 400 INCORRECT_INPUT ja teate "sequence: ei tohi olla tühi".
     */
    @Test
    void missingSequence_producesBadRequest() throws Exception {
        mockMvc.perform(putCategory("1", "{\"categoryName\":\"Aiatööd\"}").with(loggedInUser("admin")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INCORRECT_INPUT"))
                .andExpect(jsonPath("$.message").value("sequence: ei tohi olla tühi"));

        verifyNoInteractions(adminCategoryUpdateService);
    }

    /**
     * Liiga pikk nimi (101) ja liiga pikk kirjeldus (256) annavad 400; täpselt 100 ja 255 märki on lubatud.
     */
    @Test
    void tooLongFields_produceBadRequestButLimitsAreAllowed() throws Exception {
        mockMvc.perform(putCategory("1", "{\"categoryName\":\"" + "a".repeat(101) + "\",\"sequence\":1}").with(loggedInUser("admin")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("categoryName: ei tohi olla pikem kui 100 märki"));
        mockMvc.perform(putCategory("1", "{\"categoryName\":\"Uus\",\"description\":\"" + "a".repeat(256) + "\",\"sequence\":1}")
                        .with(loggedInUser("admin")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("description: ei tohi olla pikem kui 255 märki"));

        mockMvc.perform(putCategory("1", "{\"categoryName\":\"" + "a".repeat(100) + "\",\"description\":\"" + "a".repeat(255)
                        + "\",\"sequence\":1}").with(loggedInUser("admin")))
                .andExpect(status().isOk());
    }

    /**
     * Tekstiline categoryId annab 400 INCORRECT_INPUT ja service'it ei kutsuta.
     */
    @Test
    void nonIntegerCategoryId_producesBadRequest() throws Exception {
        mockMvc.perform(putCategory("abc", VALID_JSON).with(loggedInUser("admin")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INCORRECT_INPUT"));

        verifyNoInteractions(adminCategoryUpdateService);
    }

    /**
     * Olematu kategooria annab 404 PRIMARY_KEY_NOT_FOUND ja teate tegeliku ID-ga.
     */
    @Test
    void missingCategory_producesNotFound() throws Exception {
        doThrow(new PrimaryKeyNotFoundException("categoryId", 123)).when(adminCategoryUpdateService)
                .updateCategory(123, new CategoryUpdateRequestDto("Aiatööd", "Muruniidukid, labidad, rehad", 100));

        mockMvc.perform(putCategory("123", VALID_JSON).with(loggedInUser("admin")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRIMARY_KEY_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Ei leidnud primary keyd 'categoryId' väärtusega: 123"));
    }

    /**
     * Teise kategooria nimi annab 403 CATEGORY_UNAVAILABLE ApiError kujul.
     */
    @Test
    void nameOfOtherCategory_producesForbiddenWithErrorCode() throws Exception {
        doThrow(new ForbiddenException("Sellise nimega kategooria on juba olemas", "CATEGORY_UNAVAILABLE"))
                .when(adminCategoryUpdateService).updateCategory(1, new CategoryUpdateRequestDto("Ehitustööd", null, 100));

        mockMvc.perform(putCategory("1", "{\"categoryName\":\"Ehitustööd\",\"sequence\":100}").with(loggedInUser("admin")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("CATEGORY_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value("Sellise nimega kategooria on juba olemas"));
    }

    /**
     * Andmebaasi tõrge annab 500 INTERNAL_SERVER_ERROR kokkulepitud sõnumiga.
     */
    @Test
    void updatingFailure_producesInternalServerError() throws Exception {
        doThrow(new InternalServerErrorException("Kategooria muutmine ebaõnnestus. Palun proovi hiljem uuesti."))
                .when(adminCategoryUpdateService).updateCategory(1, new CategoryUpdateRequestDto("Aiatööd", null, 100));

        mockMvc.perform(putCategory("1", "{\"categoryName\":\"Aiatööd\",\"sequence\":100}").with(loggedInUser("admin")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("Kategooria muutmine ebaõnnestus. Palun proovi hiljem uuesti."));
    }

    /**
     * Koostab PUT /api/admin/categories/{categoryId} päringu antud ID ja JSON body'ga.
     * Sisselogimine lisatakse testis eraldi.
     */
    private static MockHttpServletRequestBuilder putCategory(String categoryId, String json) {
        return put("/api/admin/categories/" + categoryId).contentType(MediaType.APPLICATION_JSON).content(json);
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
