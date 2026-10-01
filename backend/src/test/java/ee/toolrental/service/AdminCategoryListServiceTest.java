package ee.toolrental.service;

import ee.toolrental.controller.admin.dto.AdminCategoryDto;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.persistence.category.AdminCategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminCategoryListServiceTest {

    private final AdminCategoryRepository adminCategoryRepository = mock(AdminCategoryRepository.class);
    private final AdminCategoryListService adminCategoryListService = new AdminCategoryListService(adminCategoryRepository);

    /**
     * Service tagastab repositooriumi kategooriad muutmata ja samas järjestuses.
     * Sisaldab kirjeldusega kategooriat ja kategooriat, mille kirjeldus on null.
     */
    @Test
    void returnsCategoriesFromRepositoryInSameOrder() {
        AdminCategoryDto gardenCategory = new AdminCategoryDto(1, "Aiatööd", "Muruniidukid, labidad, rehad", 100);
        AdminCategoryDto categoryWithoutDescription = new AdminCategoryDto(5, "Uus", null, 500);
        when(adminCategoryRepository.findAllAdminCategoriesBy()).thenReturn(List.of(gardenCategory, categoryWithoutDescription));

        List<AdminCategoryDto> result = adminCategoryListService.getCategories();

        assertEquals(List.of(gardenCategory, categoryWithoutDescription), result);
    }

    /**
     * Kategooriaid pole: tulemus on tühi loend, mitte viga.
     */
    @Test
    void noCategoriesProducesEmptyList() {
        when(adminCategoryRepository.findAllAdminCategoriesBy()).thenReturn(List.of());

        assertTrue(adminCategoryListService.getCategories().isEmpty());
    }

    /**
     * Andmebaasi tõrge muutub InternalServerErrorException'iks kokkulepitud sõnumiga, SQL-i kliendile ei näidata.
     */
    @Test
    void databaseFailureProducesInternalServerErrorWithoutTechnicalDetails() {
        when(adminCategoryRepository.findAllAdminCategoriesBy())
                .thenThrow(new DataAccessResourceFailureException("SQL details"));

        InternalServerErrorException exception = assertThrows(
                InternalServerErrorException.class, () -> adminCategoryListService.getCategories());

        assertEquals("INTERNAL_SERVER_ERROR", exception.getErrorCode());
        assertEquals("Kategooriate laadimine ebaõnnestus. Palun proovi hiljem uuesti.", exception.getMessage());
        assertFalse(exception.getMessage().contains("SQL details"));
    }
}
