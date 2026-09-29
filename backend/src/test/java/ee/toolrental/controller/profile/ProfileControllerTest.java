package ee.toolrental.controller.profile;

import ee.toolrental.controller.profile.dto.ProfileDto;
import ee.toolrental.controller.profile.dto.ProfileUpdateRequestDto;
import ee.toolrental.infrastructure.exception.ForbiddenException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.toolrental.infrastructure.security.AppUserOidcService;
import ee.toolrental.infrastructure.security.AppUserPrincipal;
import ee.toolrental.infrastructure.security.SecurityConfig;
import ee.toolrental.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "spring.security.oauth2.client.registration.google.client-id=test-client-id",
        "spring.security.oauth2.client.registration.google.client-secret=test-client-secret"})
class ProfileControllerTest {

    private static final String VALID_PROFILE_JSON = """
            {
              "firstName": "Liis",
              "lastName": "Kask",
              "email": "liis.kask@example.com",
              "phone": "55501002",
              "districtId": 2,
              "streetName": "Sõpruse pst",
              "houseNumber": "120",
              "apartmentNumber": "8"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private AppUserOidcService appUserOidcService;

    /**
     * Sisselogitud kasutaja saab GET päringuga oma profiili täpselt 11 väljaga.
     * Service'ile antakse kasutaja ID ja e-post sessioonist.
     */
    @Test
    void getProfile_loggedInUser_returnsProfileFromSessionUser() throws Exception {
        ProfileDto profileDto = new ProfileDto();
        profileDto.setUserId(3);
        profileDto.setFirstName("Liis");
        profileDto.setLastName("Kask");
        profileDto.setEmail("liis.kask@example.com");
        profileDto.setPhone("55501002");
        profileDto.setCityId(1);
        profileDto.setDistrictId(2);
        profileDto.setStreetName("Sõpruse pst");
        profileDto.setHouseNumber("120");
        profileDto.setApartmentNumber("8");
        profileDto.setHasProfile(true);
        when(profileService.getProfile(3, "liis@gmail.com")).thenReturn(profileDto);

        mockMvc.perform(get("/api/users/me/profile").with(loggedInUser(3, "liis@gmail.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(11))
                .andExpect(jsonPath("$.userId").value(3))
                .andExpect(jsonPath("$.firstName").value("Liis"))
                .andExpect(jsonPath("$.lastName").value("Kask"))
                .andExpect(jsonPath("$.email").value("liis.kask@example.com"))
                .andExpect(jsonPath("$.phone").value("55501002"))
                .andExpect(jsonPath("$.cityId").value(1))
                .andExpect(jsonPath("$.districtId").value(2))
                .andExpect(jsonPath("$.streetName").value("Sõpruse pst"))
                .andExpect(jsonPath("$.houseNumber").value("120"))
                .andExpect(jsonPath("$.apartmentNumber").value("8"))
                .andExpect(jsonPath("$.hasProfile").value(true));
        verify(profileService).getProfile(3, "liis@gmail.com");
    }

    /**
     * Profiilita kasutaja vastuses on profiili väljad null ja hasProfile false.
     * Vastuses on ikkagi kõik 11 välja.
     */
    @Test
    void getProfile_userWithoutProfile_returnsNullFields() throws Exception {
        ProfileDto profileDto = new ProfileDto();
        profileDto.setUserId(583);
        profileDto.setFirstName("Mari");
        profileDto.setLastName("Maasikas");
        profileDto.setEmail("user@gmail.com");
        profileDto.setHasProfile(false);
        when(profileService.getProfile(583, "user@gmail.com")).thenReturn(profileDto);

        mockMvc.perform(get("/api/users/me/profile").with(loggedInUser(583, "user@gmail.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(11))
                .andExpect(jsonPath("$.email").value("user@gmail.com"))
                .andExpect(jsonPath("$.phone").isEmpty())
                .andExpect(jsonPath("$.cityId").isEmpty())
                .andExpect(jsonPath("$.apartmentNumber").isEmpty())
                .andExpect(jsonPath("$.hasProfile").value(false));
    }

    /**
     * Sisse logimata kasutaja GET päring saab 401 tühja vastusega.
     * Service'it ei kutsuta.
     */
    @Test
    void getProfile_notLoggedIn_returns401() throws Exception {
        mockMvc.perform(get("/api/users/me/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));
        verifyNoInteractions(profileService);
    }

    /**
     * Profiili laadimise andmebaasi tõrge annab 500 ja ApiError vastuse.
     * errorCode on INTERNAL_SERVER_ERROR ja teade on eestikeelne.
     */
    @Test
    void getProfile_databaseFailure_returns500() throws Exception {
        when(profileService.getProfile(3, "liis@gmail.com")).thenThrow(
                new InternalServerErrorException("Profiili laadimine ebaõnnestus. Palun proovi hiljem uuesti."));

        mockMvc.perform(get("/api/users/me/profile").with(loggedInUser(3, "liis@gmail.com")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("Profiili laadimine ebaõnnestus. Palun proovi hiljem uuesti."));
    }

    /**
     * Kehtiv PUT päring annab 200 tühja vastusega.
     * Service saab kasutaja ID sessioonist ja kõik päringu väljad.
     */
    @Test
    void updateProfile_validRequest_returns200WithEmptyBody() throws Exception {
        mockMvc.perform(putProfile(VALID_PROFILE_JSON).with(loggedInUser(3, "liis@gmail.com")))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        ArgumentCaptor<ProfileUpdateRequestDto> captor = ArgumentCaptor.forClass(ProfileUpdateRequestDto.class);
        verify(profileService).updateProfile(eq(3), captor.capture());
        ProfileUpdateRequestDto profileUpdateRequestDto = captor.getValue();
        assertEquals("Liis", profileUpdateRequestDto.getFirstName());
        assertEquals("liis.kask@example.com", profileUpdateRequestDto.getEmail());
        assertEquals(2, profileUpdateRequestDto.getDistrictId());
        assertEquals("8", profileUpdateRequestDto.getApartmentNumber());
    }

    /**
     * Päringu body's antud userId ja googleSub ei mõjuta midagi.
     * Service saab alati sessiooni kasutaja ID.
     */
    @Test
    void updateProfile_userIdInBody_isIgnored() throws Exception {
        String json = VALID_PROFILE_JSON.replace("{", "{\"userId\": 1, \"googleSub\": \"võõras\",");

        mockMvc.perform(putProfile(json).with(loggedInUser(3, "liis@gmail.com")))
                .andExpect(status().isOk());

        verify(profileService).updateProfile(eq(3), any());
    }

    /**
     * Korterinumbrita päring on lubatud, sest apartmentNumber on valikuline.
     * Vastus on 200.
     */
    @Test
    void updateProfile_withoutApartmentNumber_returns200() throws Exception {
        String json = VALID_PROFILE_JSON.replace("\"apartmentNumber\": \"8\"", "\"apartmentNumber\": null");

        mockMvc.perform(putProfile(json).with(loggedInUser(3, "liis@gmail.com")))
                .andExpect(status().isOk());
    }

    /**
     * Tühi firstName annab 400 teatega "firstName: ei tohi olla tühi".
     * Service'it ei kutsuta.
     */
    @Test
    void updateProfile_emptyFirstName_returns400() throws Exception {
        String json = VALID_PROFILE_JSON.replace("\"firstName\": \"Liis\"", "\"firstName\": \"\"");

        mockMvc.perform(putProfile(json).with(loggedInUser(3, "liis@gmail.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INCORRECT_INPUT"))
                .andExpect(jsonPath("$.message").value("firstName: ei tohi olla tühi"));
        verifyNoInteractions(profileService);
    }

    /**
     * Vale e-posti vorming, puuduv districtId ja liiga pikk väli annavad 400 INCORRECT_INPUT.
     * Teade algab vigase välja nimega.
     */
    @Test
    void updateProfile_invalidFields_returns400() throws Exception {
        String invalidEmailJson = VALID_PROFILE_JSON.replace("liis.kask@example.com", "vale-email");
        mockMvc.perform(putProfile(invalidEmailJson).with(loggedInUser(3, "liis@gmail.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INCORRECT_INPUT"))
                .andExpect(jsonPath("$.message").value("email: peab olema korrektne e-posti aadress"));

        String missingDistrictJson = VALID_PROFILE_JSON.replace("\"districtId\": 2,", "");
        mockMvc.perform(putProfile(missingDistrictJson).with(loggedInUser(3, "liis@gmail.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("districtId: ei tohi olla tühi"));

        String longHouseNumberJson = VALID_PROFILE_JSON.replace("\"120\"", "\"" + "1".repeat(21) + "\"");
        mockMvc.perform(putProfile(longHouseNumberJson).with(loggedInUser(3, "liis@gmail.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("houseNumber: võib olla kuni 20 märki"));

        verifyNoInteractions(profileService);
    }

    /**
     * Teise kasutaja e-post annab 403 EMAIL_ALREADY_EXISTS.
     * Viga tuleb service'ist ForbiddenException'ina.
     */
    @Test
    void updateProfile_emailAlreadyExists_returns403() throws Exception {
        doThrow(new ForbiddenException("Sellise e-postiga kasutaja on juba süsteemis olemas", "EMAIL_ALREADY_EXISTS"))
                .when(profileService).updateProfile(eq(3), any());

        mockMvc.perform(putProfile(VALID_PROFILE_JSON).with(loggedInUser(3, "liis@gmail.com")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("EMAIL_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").value("Sellise e-postiga kasutaja on juba süsteemis olemas"));
    }

    /**
     * Olematu linnaosa annab 404 PRIMARY_KEY_NOT_FOUND koos päringu districtId väärtusega.
     * Viga tuleb service'ist PrimaryKeyNotFoundException'ina.
     */
    @Test
    void updateProfile_unknownDistrict_returns404() throws Exception {
        doThrow(new PrimaryKeyNotFoundException("districtId", 99)).when(profileService).updateProfile(eq(3), any());

        mockMvc.perform(putProfile(VALID_PROFILE_JSON.replace("\"districtId\": 2", "\"districtId\": 99"))
                        .with(loggedInUser(3, "liis@gmail.com")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRIMARY_KEY_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Ei leidnud primary keyd 'districtId' väärtusega: 99"));
    }

    /**
     * Sisse logimata kasutaja PUT päring saab 401 tühja vastusega.
     * Service'it ei kutsuta.
     */
    @Test
    void updateProfile_notLoggedIn_returns401() throws Exception {
        mockMvc.perform(putProfile(VALID_PROFILE_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));
        verifyNoInteractions(profileService);
    }

    /**
     * Profiili salvestamise andmebaasi tõrge annab 500 ja ApiError vastuse.
     * errorCode on INTERNAL_SERVER_ERROR ja teade on eestikeelne.
     */
    @Test
    void updateProfile_databaseFailure_returns500() throws Exception {
        doThrow(new InternalServerErrorException("Profiili salvestamine ebaõnnestus. Palun proovi hiljem uuesti."))
                .when(profileService).updateProfile(eq(3), any());

        mockMvc.perform(putProfile(VALID_PROFILE_JSON).with(loggedInUser(3, "liis@gmail.com")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("Profiili salvestamine ebaõnnestus. Palun proovi hiljem uuesti."));
    }

    /**
     * Koostab PUT /api/users/me/profile päringu antud JSON body'ga.
     * Sisselogimine lisatakse vajadusel testis eraldi.
     */
    private static MockHttpServletRequestBuilder putProfile(String json) {
        return put("/api/users/me/profile").contentType(MediaType.APPLICATION_JSON).content(json);
    }

    /**
     * Loob sisselogitud Google'i kasutaja, nagu AppUserOidcService selle sessiooni paneks.
     * Principal sisaldab rakenduse kasutaja ID-d ja Google'i e-posti.
     */
    private static RequestPostProcessor loggedInUser(Integer userId, String email) {
        OidcIdToken oidcIdToken = OidcIdToken.withTokenValue("test-token")
                .subject("google-sub-" + userId)
                .claim("email", email)
                .build();
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_customer"));
        AppUserPrincipal appUserPrincipal = new AppUserPrincipal(userId, authorities, oidcIdToken, null);
        return authentication(new OAuth2AuthenticationToken(appUserPrincipal, authorities, "google"));
    }
}
