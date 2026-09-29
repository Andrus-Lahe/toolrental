package ee.toolrental.service;

import ee.toolrental.controller.profile.dto.ProfileDto;
import ee.toolrental.controller.profile.dto.ProfileUpdateRequestDto;
import ee.toolrental.infrastructure.exception.ForbiddenException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.toolrental.persistence.appuser.AppUser;
import ee.toolrental.persistence.appuser.AppUserMapperImpl;
import ee.toolrental.persistence.city.City;
import ee.toolrental.persistence.district.District;
import ee.toolrental.persistence.location.Location;
import ee.toolrental.persistence.location.LocationRepository;
import ee.toolrental.persistence.profile.Profile;
import ee.toolrental.persistence.profile.ProfileMapperImpl;
import ee.toolrental.persistence.profile.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataAccessResourceFailureException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProfileServiceTest {

    private ProfileRepository profileRepository;
    private LocationRepository locationRepository;
    private AppUserService appUserService;
    private DistrictService districtService;
    private ProfileService profileService;

    /**
     * Loob iga testi jaoks uued mock'id ja päris MapStructi mapperid.
     * Nii kontrollitakse service'i loogikat koos tegeliku andmete teisendamisega.
     */
    @BeforeEach
    void setUp() {
        profileRepository = mock(ProfileRepository.class);
        locationRepository = mock(LocationRepository.class);
        appUserService = mock(AppUserService.class);
        districtService = mock(DistrictService.class);
        profileService = new ProfileService(profileRepository, locationRepository, new ProfileMapperImpl(),
                new AppUserMapperImpl(), appUserService, districtService);
    }

    /**
     * Profiiliga kasutaja (Liis Kask) saab kõik profiili ja aadressi väljad.
     * app_user tabelisse eraldi ei pöörduta, sest nimi tuleb profiili kaudu.
     */
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

    /**
     * Korterinumbrita aadressiga kasutaja (Marko Tamm) saab apartmentNumber = null.
     * Linnaosa ja hasProfile väärtus tulevad profiili aadressist.
     */
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

    /**
     * Profiilita kasutaja saab nime app_user tabelist ja e-posti sessioonist.
     * Kõik profiili ja aadressi väljad on null ning hasProfile on false.
     */
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

    /**
     * Andmebaasi tõrge profiili laadimisel muudetakse InternalServerErrorException'iks.
     * Veakood ja teade vastavad taski 500 vastusele.
     */
    @Test
    void getProfile_databaseFailure_throwsInternalServerErrorException() {
        when(profileRepository.findProfileBy(3)).thenThrow(new DataAccessResourceFailureException("DB down"));

        InternalServerErrorException exception =
                assertThrows(InternalServerErrorException.class, () -> profileService.getProfile(3, "user@gmail.com"));

        assertEquals("INTERNAL_SERVER_ERROR", exception.getErrorCode());
        assertEquals("Profiili laadimine ebaõnnestus. Palun proovi hiljem uuesti.", exception.getMessage());
    }

    /**
     * Profiilita kasutajale luuakse uus aadress ja profiil ning uuendatakse nimi.
     * Uuel aadressil on lng ja lat null, profiilil created_at ja updated_at täidetud.
     */
    @Test
    void updateProfile_userWithoutProfile_createsLocationAndProfile() {
        AppUser appUser = createAppUser(583, "Mari", "Maasikas");
        District district = createDistrict(1, 2);
        when(districtService.getValidDistrictBy(2)).thenReturn(district);
        when(appUserService.getValidAppUserBy(583)).thenReturn(appUser);
        when(profileRepository.findProfileBy(583)).thenReturn(Optional.empty());

        profileService.updateProfile(583, createProfileUpdateRequestDto("mari@example.com", 2, "8"));

        ArgumentCaptor<Location> locationCaptor = ArgumentCaptor.forClass(Location.class);
        verify(locationRepository).save(locationCaptor.capture());
        Location location = locationCaptor.getValue();
        assertSame(district, location.getDistrict());
        assertEquals("Sõpruse pst", location.getStreetName());
        assertEquals("120", location.getHouseNumber());
        assertEquals("8", location.getApartmentNumber());
        assertNull(location.getLng());
        assertNull(location.getLat());

        ArgumentCaptor<Profile> profileCaptor = ArgumentCaptor.forClass(Profile.class);
        verify(profileRepository).saveAndFlush(profileCaptor.capture());
        Profile profile = profileCaptor.getValue();
        assertSame(appUser, profile.getUser());
        assertSame(location, profile.getLocation());
        assertEquals("mari@example.com", profile.getEmail());
        assertEquals("55501002", profile.getPhone());
        assertNotNull(profile.getCreatedAt());
        assertEquals(profile.getCreatedAt(), profile.getUpdatedAt());

        assertEquals("Liis", appUser.getFirstName());
        assertEquals("Kask", appUser.getLastName());
    }

    /**
     * Profiiliga kasutajal uuendatakse olemasolevat profiili ja aadressi, uut aadressi ei looda.
     * created_at, lng ja lat jäävad muutmata, updated_at ja linnaosa muutuvad.
     */
    @Test
    void updateProfile_userWithProfile_updatesExistingProfileAndLocation() {
        Profile profile = createProfile(3, "Vana", "Nimi", "vana@example.com", "111", 1, 1, "Vana tn", "1", "2");
        Instant createdAt = Instant.parse("2026-01-01T10:00:00Z");
        profile.setCreatedAt(createdAt);
        profile.setUpdatedAt(createdAt);
        profile.getLocation().setLng(new BigDecimal("24.7000000"));
        profile.getLocation().setLat(new BigDecimal("59.4000000"));
        District district = createDistrict(1, 2);
        when(districtService.getValidDistrictBy(2)).thenReturn(district);
        when(appUserService.getValidAppUserBy(3)).thenReturn(profile.getUser());
        when(profileRepository.findProfileBy(3)).thenReturn(Optional.of(profile));

        profileService.updateProfile(3, createProfileUpdateRequestDto("liis.kask@example.com", 2, "8"));

        verify(locationRepository, never()).save(any());
        verify(profileRepository).saveAndFlush(profile);
        assertEquals("liis.kask@example.com", profile.getEmail());
        assertEquals("55501002", profile.getPhone());
        assertEquals(createdAt, profile.getCreatedAt());
        assertTrue(profile.getUpdatedAt().isAfter(createdAt));
        assertSame(district, profile.getLocation().getDistrict());
        assertEquals("Sõpruse pst", profile.getLocation().getStreetName());
        assertEquals("120", profile.getLocation().getHouseNumber());
        assertEquals("8", profile.getLocation().getApartmentNumber());
        assertEquals(new BigDecimal("24.7000000"), profile.getLocation().getLng());
        assertEquals(new BigDecimal("59.4000000"), profile.getLocation().getLat());
        assertEquals("Liis", profile.getUser().getFirstName());
        assertEquals("Kask", profile.getUser().getLastName());
    }

    /**
     * Tühi string või null korterinumbrina salvestatakse null väärtusena.
     * Kontrollitakse olemasoleva aadressi uuendamise kaudu.
     */
    @Test
    void updateProfile_blankOrNullApartmentNumber_savesNull() {
        for (String apartmentNumber : new String[]{"", "  ", null}) {
            Profile profile = createProfile(3, "Liis", "Kask", "liis.kask@example.com", "55501002", 1, 2, "Sõpruse pst", "120", "8");
            when(districtService.getValidDistrictBy(2)).thenReturn(createDistrict(1, 2));
            when(appUserService.getValidAppUserBy(3)).thenReturn(profile.getUser());
            when(profileRepository.findProfileBy(3)).thenReturn(Optional.of(profile));

            profileService.updateProfile(3, createProfileUpdateRequestDto("liis.kask@example.com", 2, apartmentNumber));

            assertNull(profile.getLocation().getApartmentNumber());
        }
    }

    /**
     * Kasutaja oma senise e-posti uuesti salvestamine on lubatud.
     * E-posti kontroll küsib ainult teiste kasutajate profiile, seega viga ei teki.
     */
    @Test
    void updateProfile_ownEmail_isAllowed() {
        Profile profile = createProfile(3, "Liis", "Kask", "liis.kask@example.com", "55501002", 1, 2, "Sõpruse pst", "120", "8");
        when(districtService.getValidDistrictBy(2)).thenReturn(createDistrict(1, 2));
        when(profileRepository.existsOtherUserProfileBy("liis.kask@example.com", 3)).thenReturn(false);
        when(appUserService.getValidAppUserBy(3)).thenReturn(profile.getUser());
        when(profileRepository.findProfileBy(3)).thenReturn(Optional.of(profile));

        assertDoesNotThrow(() -> profileService.updateProfile(3, createProfileUpdateRequestDto("liis.kask@example.com", 2, "8")));

        verify(profileRepository).existsOtherUserProfileBy("liis.kask@example.com", 3);
        verify(profileRepository).saveAndFlush(profile);
    }

    /**
     * Teise kasutaja e-post annab ForbiddenException'i koodiga EMAIL_ALREADY_EXISTS.
     * Midagi ei salvestata ja kasutaja nime ei muudeta.
     */
    @Test
    void updateProfile_otherUsersEmail_throwsForbiddenException() {
        when(districtService.getValidDistrictBy(2)).thenReturn(createDistrict(1, 2));
        when(profileRepository.existsOtherUserProfileBy("email@Gmail.com", 3)).thenReturn(true);

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> profileService.updateProfile(3, createProfileUpdateRequestDto("email@Gmail.com", 2, "8")));

        assertEquals("EMAIL_ALREADY_EXISTS", exception.getErrorCode());
        assertEquals("Sellise e-postiga kasutaja on juba süsteemis olemas", exception.getMessage());
        verifyNoInteractions(appUserService, locationRepository);
        verify(profileRepository, never()).saveAndFlush(any());
    }

    /**
     * Olematu linnaosa annab PrimaryKeyNotFoundException'i päringu districtId väärtusega.
     * Linnaosa kontroll on enne e-posti kontrolli ja midagi ei salvestata.
     */
    @Test
    void updateProfile_unknownDistrict_throwsPrimaryKeyNotFoundException() {
        when(districtService.getValidDistrictBy(99)).thenThrow(new PrimaryKeyNotFoundException("districtId", 99));

        PrimaryKeyNotFoundException exception = assertThrows(PrimaryKeyNotFoundException.class,
                () -> profileService.updateProfile(3, createProfileUpdateRequestDto("liis.kask@example.com", 99, "8")));

        assertEquals("Ei leidnud primary keyd 'districtId' väärtusega: 99", exception.getMessage());
        verifyNoInteractions(profileRepository, appUserService, locationRepository);
    }

    /**
     * Andmebaasi tõrge salvestamisel muudetakse InternalServerErrorException'iks.
     * Veakood ja teade vastavad taski 500 vastusele.
     */
    @Test
    void updateProfile_databaseFailure_throwsInternalServerErrorException() {
        Profile profile = createProfile(3, "Liis", "Kask", "liis.kask@example.com", "55501002", 1, 2, "Sõpruse pst", "120", "8");
        when(districtService.getValidDistrictBy(2)).thenReturn(createDistrict(1, 2));
        when(appUserService.getValidAppUserBy(3)).thenReturn(profile.getUser());
        when(profileRepository.findProfileBy(3)).thenReturn(Optional.of(profile));
        when(profileRepository.saveAndFlush(profile)).thenThrow(new DataAccessResourceFailureException("DB down"));

        InternalServerErrorException exception = assertThrows(InternalServerErrorException.class,
                () -> profileService.updateProfile(3, createProfileUpdateRequestDto("liis.kask@example.com", 2, "8")));

        assertEquals("INTERNAL_SERVER_ERROR", exception.getErrorCode());
        assertEquals("Profiili salvestamine ebaõnnestus. Palun proovi hiljem uuesti.", exception.getMessage());
    }

    /**
     * Koostab profiili salvestamise päringu Liis Kase näite andmetega.
     * E-post, linnaosa ja korterinumber antakse testist ette.
     */
    private static ProfileUpdateRequestDto createProfileUpdateRequestDto(String email, Integer districtId, String apartmentNumber) {
        ProfileUpdateRequestDto profileUpdateRequestDto = new ProfileUpdateRequestDto();
        profileUpdateRequestDto.setFirstName("Liis");
        profileUpdateRequestDto.setLastName("Kask");
        profileUpdateRequestDto.setEmail(email);
        profileUpdateRequestDto.setPhone("55501002");
        profileUpdateRequestDto.setDistrictId(districtId);
        profileUpdateRequestDto.setStreetName("Sõpruse pst");
        profileUpdateRequestDto.setHouseNumber("120");
        profileUpdateRequestDto.setApartmentNumber(apartmentNumber);
        return profileUpdateRequestDto;
    }

    /**
     * Koostab profiili koos kasutaja, aadressi, linnaosa ja linnaga.
     * Kasutatakse mock-repositooriumi tagastusväärtusena.
     */
    private static Profile createProfile(Integer userId, String firstName, String lastName, String email, String phone,
                                         Integer cityId, Integer districtId, String streetName, String houseNumber,
                                         String apartmentNumber) {
        Location location = new Location();
        location.setDistrict(createDistrict(cityId, districtId));
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

    /**
     * Koostab linnaosa, mis kuulub antud ID-ga linna.
     * Nimed pole testide jaoks olulised ja jäävad täitmata.
     */
    private static District createDistrict(Integer cityId, Integer districtId) {
        City city = new City();
        city.setId(cityId);

        District district = new District();
        district.setId(districtId);
        district.setCity(city);
        return district;
    }

    /**
     * Koostab kasutaja antud ID ja nimega.
     * google_sub tuletatakse ID-st, roll ja status jäävad täitmata.
     */
    private static AppUser createAppUser(Integer userId, String firstName, String lastName) {
        AppUser appUser = new AppUser();
        appUser.setId(userId);
        appUser.setFirstName(firstName);
        appUser.setLastName(lastName);
        appUser.setGoogleSub("google-sub-" + userId);
        return appUser;
    }
}
