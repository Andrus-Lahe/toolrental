package ee.toolrental.service;

import ee.toolrental.controller.profile.dto.ProfileDto;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.persistence.appuser.AppUser;
import ee.toolrental.persistence.city.City;
import ee.toolrental.persistence.district.District;
import ee.toolrental.persistence.location.Location;
import ee.toolrental.persistence.profile.Profile;
import ee.toolrental.persistence.profile.ProfileMapper;
import ee.toolrental.persistence.profile.ProfileMapperImpl;
import ee.toolrental.persistence.profile.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfileServiceTest {

    private ProfileRepository profileRepository;
    private AppUserService appUserService;
    private ProfileService profileService;

    @BeforeEach
    void setUp() {
        profileRepository = mock(ProfileRepository.class);
        appUserService = mock(AppUserService.class);
        ProfileMapper profileMapper = new ProfileMapperImpl();
        profileService = new ProfileService(profileRepository, profileMapper, appUserService);
    }

    @Test
    void getProfile_userWithProfile_returnsProfileData() {
        Profile profile = createProfile(3, "Liis", "Kask", "liis.kask@example.com", "55501002",
                1, 2, "Sõpruse pst", "120", "8");
        when(profileRepository.findProfileBy(3)).thenReturn(Optional.of(profile));

        ProfileDto profileDto = profileService.getProfile(3, "google@gmail.com");

        assertEquals(3, profileDto.getUserId());
        assertEquals("Liis", profileDto.getFirstName());
        assertEquals("Kask", profileDto.getLastName());
        assertEquals("liis.kask@example.com", profileDto.getEmail());
        assertEquals("55501002", profileDto.getPhone());
        assertEquals(1, profileDto.getCityId());
        assertEquals(2, profileDto.getDistrictId());
        assertEquals("Sõpruse pst", profileDto.getStreetName());
        assertEquals("120", profileDto.getHouseNumber());
        assertEquals("8", profileDto.getApartmentNumber());
        assertTrue(profileDto.getHasProfile());
        verifyNoInteractions(appUserService);
    }

    @Test
    void getProfile_profileWithoutApartmentNumber_returnsNullApartmentNumber() {
        Profile profile = createProfile(1, "Marko", "Tamm", "email@Gmail.com", "56565656",
                1, 1, "Teddre", "28", null);
        when(profileRepository.findProfileBy(1)).thenReturn(Optional.of(profile));

        ProfileDto profileDto = profileService.getProfile(1, "email@Gmail.com");

        assertEquals(1, profileDto.getDistrictId());
        assertNull(profileDto.getApartmentNumber());
        assertTrue(profileDto.getHasProfile());
    }

    @Test
    void getProfile_userWithoutProfile_returnsAppUserNameAndSessionEmail() {
        AppUser appUser = createAppUser(583, "Mari", "Maasikas");
        when(profileRepository.findProfileBy(583)).thenReturn(Optional.empty());
        when(appUserService.getValidAppUserBy(583)).thenReturn(appUser);

        ProfileDto profileDto = profileService.getProfile(583, "user@gmail.com");

        assertEquals(583, profileDto.getUserId());
        assertEquals("Mari", profileDto.getFirstName());
        assertEquals("Maasikas", profileDto.getLastName());
        assertEquals("user@gmail.com", profileDto.getEmail());
        assertNull(profileDto.getPhone());
        assertNull(profileDto.getCityId());
        assertNull(profileDto.getDistrictId());
        assertNull(profileDto.getStreetName());
        assertNull(profileDto.getHouseNumber());
        assertNull(profileDto.getApartmentNumber());
        assertFalse(profileDto.getHasProfile());
    }

    @Test
    void getProfile_databaseFailure_throwsInternalServerErrorException() {
        when(profileRepository.findProfileBy(3)).thenThrow(new DataAccessResourceFailureException("DB down"));

        InternalServerErrorException exception =
                assertThrows(InternalServerErrorException.class, () -> profileService.getProfile(3, "user@gmail.com"));

        assertEquals("INTERNAL_SERVER_ERROR", exception.getErrorCode());
        assertEquals("Profiili laadimine ebaõnnestus. Palun proovi hiljem uuesti.", exception.getMessage());
    }

    private static Profile createProfile(Integer userId, String firstName, String lastName, String email, String phone,
                                         Integer cityId, Integer districtId, String streetName, String houseNumber,
                                         String apartmentNumber) {
        City city = new City();
        city.setId(cityId);

        District district = new District();
        district.setId(districtId);
        district.setCity(city);

        Location location = new Location();
        location.setDistrict(district);
        location.setStreetName(streetName);
        location.setHouseNumber(houseNumber);
        location.setApartmentNumber(apartmentNumber);

        Profile profile = new Profile();
        profile.setUser(createAppUser(userId, firstName, lastName));
        profile.setLocation(location);
        profile.setEmail(email);
        profile.setPhone(phone);
        return profile;
    }

    private static AppUser createAppUser(Integer userId, String firstName, String lastName) {
        AppUser appUser = new AppUser();
        appUser.setId(userId);
        appUser.setFirstName(firstName);
        appUser.setLastName(lastName);
        appUser.setGoogleSub("google-sub-" + userId);
        return appUser;
    }
}
