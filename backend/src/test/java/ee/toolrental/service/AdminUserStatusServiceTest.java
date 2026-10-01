package ee.toolrental.service;

import ee.toolrental.controller.admin.dto.UserStatusRequestDto;
import ee.toolrental.infrastructure.exception.ForbiddenException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.toolrental.persistence.appuser.AppUser;
import ee.toolrental.persistence.appuser.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

class AdminUserStatusServiceTest {

    private final AppUserRepository appUserRepository = mock(AppUserRepository.class);
    private final AppUserService appUserService = mock(AppUserService.class);
    private final AdminUserStatusService adminUserStatusService =
            new AdminUserStatusService(appUserRepository, appUserService);

    /**
     * Teise kasutaja blokeerimine seab oleku B ja salvestab selle kohe (saveAndFlush).
     */
    @Test
    void blockingOtherUser_setsStatusBAndSaves() {
        AppUser appUser = appUser("A");
        when(appUserService.getValidAppUserBy(3)).thenReturn(appUser);

        adminUserStatusService.changeUserStatus(1, 3, new UserStatusRequestDto("B"));

        assertEquals("B", appUser.getStatus());
        verify(appUserRepository).saveAndFlush(appUser);
    }

    /**
     * Aktiveerimine (A) taastab blokeeritud kasutaja oleku.
     */
    @Test
    void activatingBlockedUser_setsStatusA() {
        AppUser appUser = appUser("B");
        when(appUserService.getValidAppUserBy(3)).thenReturn(appUser);

        adminUserStatusService.changeUserStatus(1, 3, new UserStatusRequestDto("A"));

        assertEquals("A", appUser.getStatus());
        verify(appUserRepository).saveAndFlush(appUser);
    }

    /**
     * Sama oleku uuesti saatmine on idempotentne: viga ei teki ja olek jääb samaks.
     */
    @Test
    void sameStatusAgain_isIdempotent() {
        AppUser appUser = appUser("B");
        when(appUserService.getValidAppUserBy(3)).thenReturn(appUser);

        adminUserStatusService.changeUserStatus(1, 3, new UserStatusRequestDto("B"));

        assertEquals("B", appUser.getStatus());
    }

    /**
     * Olematu kasutaja annab 404 PRIMARY_KEY_NOT_FOUND tegeliku ID-ga ja midagi ei salvestata.
     */
    @Test
    void missingUser_producesPrimaryKeyNotFoundAndSavesNothing() {
        when(appUserService.getValidAppUserBy(123)).thenThrow(new PrimaryKeyNotFoundException("userId", 123));

        PrimaryKeyNotFoundException exception = assertThrows(PrimaryKeyNotFoundException.class,
                () -> adminUserStatusService.changeUserStatus(1, 123, new UserStatusRequestDto("B")));

        assertEquals("Ei leidnud primary keyd 'userId' väärtusega: 123", exception.getMessage());
        verify(appUserRepository, never()).saveAndFlush(any());
    }

    /**
     * Admin ei saa iseennast blokeerida: 403 SELF_BLOCK_NOT_ALLOWED ja olek jääb muutmata.
     */
    @Test
    void blockingSelf_producesSelfBlockNotAllowedAndKeepsStatus() {
        AppUser appUser = appUser("A");
        when(appUserService.getValidAppUserBy(1)).thenReturn(appUser);

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> adminUserStatusService.changeUserStatus(1, 1, new UserStatusRequestDto("B")));

        assertEquals("SELF_BLOCK_NOT_ALLOWED", exception.getErrorCode());
        assertEquals("Iseennast ei saa blokeerida", exception.getMessage());
        assertEquals("A", appUser.getStatus());
        verify(appUserRepository, never()).saveAndFlush(any());
    }

    /**
     * Enda aktiveerimine (A) on lubatud, sest see ei lukusta kontot välja.
     */
    @Test
    void activatingSelf_isAllowed() {
        AppUser appUser = appUser("A");
        when(appUserService.getValidAppUserBy(1)).thenReturn(appUser);

        adminUserStatusService.changeUserStatus(1, 1, new UserStatusRequestDto("A"));

        verify(appUserRepository).saveAndFlush(appUser);
    }

    /**
     * Andmebaasi tõrge muutub 500 InternalServerErrorException'iks kokkulepitud sõnumiga, SQL-i kliendile ei näidata.
     */
    @Test
    void databaseFailure_producesInternalServerErrorWithoutTechnicalDetails() {
        AppUser appUser = appUser("A");
        when(appUserService.getValidAppUserBy(3)).thenReturn(appUser);
        when(appUserRepository.saveAndFlush(appUser)).thenThrow(new DataAccessResourceFailureException("SQL details"));

        InternalServerErrorException exception = assertThrows(InternalServerErrorException.class,
                () -> adminUserStatusService.changeUserStatus(1, 3, new UserStatusRequestDto("B")));

        assertEquals("INTERNAL_SERVER_ERROR", exception.getErrorCode());
        assertEquals("Kasutaja oleku muutmine ebaõnnestus. Palun proovi hiljem uuesti.", exception.getMessage());
        assertFalse(exception.getMessage().contains("SQL details"));
    }

    /**
     * Koostab testis kasutaja antud algolekuga.
     */
    private AppUser appUser(String status) {
        AppUser appUser = new AppUser();
        appUser.setStatus(status);
        return appUser;
    }
}
