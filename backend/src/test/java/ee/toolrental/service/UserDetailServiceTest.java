package ee.toolrental.service;

import ee.toolrental.controller.appuser.dto.UserDetailResponse;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.toolrental.persistence.appuser.AppUser;
import ee.toolrental.persistence.appuser.UserDetailMapperImpl;
import ee.toolrental.persistence.profile.Profile;
import ee.toolrental.persistence.profile.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDetailServiceTest {

    private AppUserService appUserService;
    private ProfileRepository profileRepository;
    private UserDetailService userDetailService;

    @BeforeEach
    void setUp() {
        appUserService = mock(AppUserService.class);
        profileRepository = mock(ProfileRepository.class);
        userDetailService = new UserDetailService(appUserService, profileRepository, new UserDetailMapperImpl());
    }

    @Test
    void getUserDetails_userWithProfile_returnsOnlyContactFields() {
        AppUser appUser = createAppUser(1, "Marko", "Tamm", "A");
        Profile profile = new Profile();
        profile.setEmail("email@Gmail.com");
        profile.setPhone("56565656");
        when(appUserService.getValidAppUserBy(1)).thenReturn(appUser);
        when(profileRepository.findProfileByUserId(1)).thenReturn(Optional.of(profile));

        UserDetailResponse response = userDetailService.getUserDetails(1);

        assertEquals(1, response.getUserId());
        assertEquals("Marko", response.getFirstName());
        assertEquals("Tamm", response.getLastName());
        assertEquals("email@Gmail.com", response.getEmail());
        assertEquals("56565656", response.getPhone());
    }

    @Test
    void getUserDetails_userWithoutProfile_returnsNullEmailAndPhone() {
        when(appUserService.getValidAppUserBy(4)).thenReturn(createAppUser(4, "Mari", "Mets", "A"));
        when(profileRepository.findProfileByUserId(4)).thenReturn(Optional.empty());

        UserDetailResponse response = userDetailService.getUserDetails(4);

        assertEquals("Mari", response.getFirstName());
        assertNull(response.getEmail());
        assertNull(response.getPhone());
    }

    @Test
    void getUserDetails_blockedUserStillReturnsContacts() {
        when(appUserService.getValidAppUserBy(8)).thenReturn(createAppUser(8, "Kati", "Kask", "B"));
        Profile profile = new Profile();
        profile.setEmail("kati@example.com");
        profile.setPhone("55512345");
        when(profileRepository.findProfileByUserId(8)).thenReturn(Optional.of(profile));

        UserDetailResponse response = userDetailService.getUserDetails(8);

        assertEquals("kati@example.com", response.getEmail());
        assertEquals("55512345", response.getPhone());
    }

    @Test
    void getUserDetails_unknownUser_preservesNotFoundException() {
        when(appUserService.getValidAppUserBy(123)).thenThrow(new PrimaryKeyNotFoundException("userId", 123));

        PrimaryKeyNotFoundException exception = assertThrows(PrimaryKeyNotFoundException.class,
                () -> userDetailService.getUserDetails(123));

        assertEquals("Ei leidnud primary keyd 'userId' väärtusega: 123", exception.getMessage());
        verifyNoInteractions(profileRepository);
    }

    @Test
    void getUserDetails_databaseFailure_usesTaskError() {
        when(appUserService.getValidAppUserBy(1)).thenReturn(createAppUser(1, "Marko", "Tamm", "A"));
        when(profileRepository.findProfileByUserId(1))
                .thenThrow(new DataAccessResourceFailureException("DB unavailable"));

        InternalServerErrorException exception = assertThrows(InternalServerErrorException.class,
                () -> userDetailService.getUserDetails(1));

        assertEquals("INTERNAL_SERVER_ERROR", exception.getErrorCode());
        assertEquals("Kasutaja andmete laadimine ebaõnnestus. Palun proovi hiljem uuesti.", exception.getMessage());
    }

    private AppUser createAppUser(Integer userId, String firstName, String lastName, String status) {
        AppUser appUser = new AppUser();
        appUser.setId(userId);
        appUser.setFirstName(firstName);
        appUser.setLastName(lastName);
        appUser.setStatus(status);
        return appUser;
    }
}
