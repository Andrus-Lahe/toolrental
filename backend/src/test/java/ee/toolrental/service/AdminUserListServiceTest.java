package ee.toolrental.service;

import ee.toolrental.controller.admin.dto.AdminUserDto;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.persistence.appuser.AdminUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminUserListServiceTest {

    private final AdminUserRepository adminUserRepository = mock(AdminUserRepository.class);
    private final AdminUserListService adminUserListService = new AdminUserListService(adminUserRepository);

    /**
     * Service tagastab repositooriumi kasutajad muutmata ja samas järjestuses.
     * Sisaldab profiiliga aktiivset ja profiilita blokeeritud kasutajat.
     */
    @Test
    void returnsUsersFromRepositoryInSameOrder() {
        AdminUserDto activeUser = new AdminUserDto(1, "Marko", "Tamm", "email@Gmail.com", "admin",
                LocalDate.of(2026, 9, 18), "A");
        AdminUserDto blockedUserWithoutProfile = new AdminUserDto(7, "Mari", "", null, "customer", null, "B");
        when(adminUserRepository.findAllUsersWithProfilesBy()).thenReturn(List.of(activeUser, blockedUserWithoutProfile));

        List<AdminUserDto> result = adminUserListService.getUsers();

        assertEquals(List.of(activeUser, blockedUserWithoutProfile), result);
    }

    /**
     * Kasutajaid pole: tulemus on tühi loend, mitte viga.
     */
    @Test
    void noUsersProducesEmptyList() {
        when(adminUserRepository.findAllUsersWithProfilesBy()).thenReturn(List.of());

        assertTrue(adminUserListService.getUsers().isEmpty());
    }

    /**
     * Andmebaasi tõrge muutub InternalServerErrorException'iks kokkulepitud sõnumiga.
     * Tehnilised detailid (SQL) kliendile ei jõua.
     */
    @Test
    void databaseFailureProducesInternalServerErrorWithoutTechnicalDetails() {
        when(adminUserRepository.findAllUsersWithProfilesBy())
                .thenThrow(new DataAccessResourceFailureException("SQL details"));

        InternalServerErrorException exception = assertThrows(
                InternalServerErrorException.class, () -> adminUserListService.getUsers());

        assertEquals("INTERNAL_SERVER_ERROR", exception.getErrorCode());
        assertEquals("Kasutajate laadimine ebaõnnestus. Palun proovi hiljem uuesti.", exception.getMessage());
        assertFalse(exception.getMessage().contains("SQL details"));
    }
}
