package ee.toolrental.controller.profile;

import ee.toolrental.controller.profile.dto.ProfileDto;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.infrastructure.security.AppUserOidcService;
import ee.toolrental.infrastructure.security.AppUserPrincipal;
import ee.toolrental.infrastructure.security.SecurityConfig;
import ee.toolrental.service.ProfileService;
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

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "spring.security.oauth2.client.registration.google.client-id=test-client-id",
        "spring.security.oauth2.client.registration.google.client-secret=test-client-secret"})
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private AppUserOidcService appUserOidcService;

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

    @Test
    void getProfile_notLoggedIn_returns401() throws Exception {
        mockMvc.perform(get("/api/users/me/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));
        verifyNoInteractions(profileService);
    }

    @Test
    void getProfile_databaseFailure_returns500() throws Exception {
        when(profileService.getProfile(3, "liis@gmail.com")).thenThrow(
                new InternalServerErrorException("Profiili laadimine ebaõnnestus. Palun proovi hiljem uuesti."));

        mockMvc.perform(get("/api/users/me/profile").with(loggedInUser(3, "liis@gmail.com")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("Profiili laadimine ebaõnnestus. Palun proovi hiljem uuesti."));
    }

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
