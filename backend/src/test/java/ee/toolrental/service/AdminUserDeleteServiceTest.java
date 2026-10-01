package ee.toolrental.service;

import ee.toolrental.infrastructure.exception.ForbiddenException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.toolrental.persistence.appuser.AdminUserDeleteRepository;
import ee.toolrental.persistence.appuser.AppUser;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.dao.DataAccessResourceFailureException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class AdminUserDeleteServiceTest {

    private final AdminUserDeleteRepository adminUserDeleteRepository = mock(AdminUserDeleteRepository.class);
    private final AppUserService appUserService = mock(AppUserService.class);
    private final AdminUserDeleteService adminUserDeleteService =
            new AdminUserDeleteService(adminUserDeleteRepository, appUserService);

    /**
     * Tööriistade ja broneeringuteta kasutaja kustutatakse: kõigepealt profiil, seejärel app_user.
     * Järjekord on oluline, sest profile.user_id välisvõtmel puudub ON DELETE CASCADE.
     */
    @Test
    void userWithoutData_deletesProfileBeforeUser() {
        when(appUserService.getValidAppUserBy(9)).thenReturn(new AppUser());
        when(adminUserDeleteRepository.deleteProfileOf(9)).thenReturn(1);
        when(adminUserDeleteRepository.deleteAppUserBy(9)).thenReturn(1);

        adminUserDeleteService.deleteUser(1, 9);

        InOrder inOrder = inOrder(adminUserDeleteRepository);
        inOrder.verify(adminUserDeleteRepository).deleteProfileOf(9);
        inOrder.verify(adminUserDeleteRepository).deleteAppUserBy(9);
    }

    /**
     * Profiilita kasutaja kustutamine õnnestub samuti: profiili kustutamine annab 0 rida, kasutaja kustutatakse ikkagi.
     */
    @Test
    void userWithoutProfile_isStillDeleted() {
        when(appUserService.getValidAppUserBy(9)).thenReturn(new AppUser());
        when(adminUserDeleteRepository.deleteProfileOf(9)).thenReturn(0);
        when(adminUserDeleteRepository.deleteAppUserBy(9)).thenReturn(1);

        adminUserDeleteService.deleteUser(1, 9);

        verify(adminUserDeleteRepository).deleteAppUserBy(9);
    }

    /**
     * Olematu kasutaja annab 404 PRIMARY_KEY_NOT_FOUND tegeliku ID-ga ja midagi ei kustutata.
     */
    @Test
    void missingUser_producesPrimaryKeyNotFoundAndDeletesNothing() {
        when(appUserService.getValidAppUserBy(123)).thenThrow(new PrimaryKeyNotFoundException("userId", 123));

        PrimaryKeyNotFoundException exception = assertThrows(
                PrimaryKeyNotFoundException.class, () -> adminUserDeleteService.deleteUser(1, 123));

        assertEquals("Ei leidnud primary keyd 'userId' väärtusega: 123", exception.getMessage());
        verifyNoMoreInteractions(adminUserDeleteRepository);
    }

    /**
     * Admin ei saa iseennast kustutada: 403 SELF_DELETE_NOT_ALLOWED ja andmeid ei puudutata.
     */
    @Test
    void deletingSelf_producesSelfDeleteNotAllowed() {
        when(appUserService.getValidAppUserBy(1)).thenReturn(new AppUser());

        ForbiddenException exception = assertThrows(
                ForbiddenException.class, () -> adminUserDeleteService.deleteUser(1, 1));

        assertEquals("SELF_DELETE_NOT_ALLOWED", exception.getErrorCode());
        assertEquals("Iseennast ei saa kustutada", exception.getMessage());
        verifyNoMoreInteractions(adminUserDeleteRepository);
    }

    /**
     * Kasutaja, kellel on tööriist, annab 403 USER_HAS_DATA ja midagi ei kustutata.
     */
    @Test
    void userWithTool_producesUserHasData() {
        when(appUserService.getValidAppUserBy(3)).thenReturn(new AppUser());
        when(adminUserDeleteRepository.existsToolOwnedBy(3)).thenReturn(true);

        ForbiddenException exception = assertThrows(
                ForbiddenException.class, () -> adminUserDeleteService.deleteUser(1, 3));

        assertEquals("USER_HAS_DATA", exception.getErrorCode());
        assertEquals("Kasutajat ei saa kustutada, sest tal on tööriistu või broneeringuid", exception.getMessage());
        verify(adminUserDeleteRepository, never()).deleteProfileOf(3);
        verify(adminUserDeleteRepository, never()).deleteAppUserBy(3);
    }

    /**
     * Kasutaja, kellel on ainult broneering (tööriistu pole), annab samuti 403 USER_HAS_DATA.
     */
    @Test
    void userWithBookingOnly_producesUserHasData() {
        when(appUserService.getValidAppUserBy(4)).thenReturn(new AppUser());
        when(adminUserDeleteRepository.existsToolOwnedBy(4)).thenReturn(false);
        when(adminUserDeleteRepository.existsBookingRentedBy(4)).thenReturn(true);

        ForbiddenException exception = assertThrows(
                ForbiddenException.class, () -> adminUserDeleteService.deleteUser(1, 4));

        assertEquals("USER_HAS_DATA", exception.getErrorCode());
        verify(adminUserDeleteRepository, never()).deleteAppUserBy(4);
    }

    /**
     * Andmebaasi tõrge muutub 500 InternalServerErrorException'iks kokkulepitud sõnumiga, SQL-i kliendile ei näidata.
     */
    @Test
    void databaseFailure_producesInternalServerErrorWithoutTechnicalDetails() {
        when(appUserService.getValidAppUserBy(9)).thenReturn(new AppUser());
        when(adminUserDeleteRepository.deleteProfileOf(9)).thenThrow(new DataAccessResourceFailureException("SQL details"));

        InternalServerErrorException exception = assertThrows(
                InternalServerErrorException.class, () -> adminUserDeleteService.deleteUser(1, 9));

        assertEquals("INTERNAL_SERVER_ERROR", exception.getErrorCode());
        assertEquals("Kasutaja kustutamine ebaõnnestus. Palun proovi hiljem uuesti.", exception.getMessage());
        assertFalse(exception.getMessage().contains("SQL details"));
    }
}
