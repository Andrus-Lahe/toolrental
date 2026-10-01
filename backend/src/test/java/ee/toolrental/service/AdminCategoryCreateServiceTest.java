package ee.toolrental.service;

import ee.toolrental.controller.admin.dto.CategoryCreateRequestDto;
import ee.toolrental.infrastructure.exception.ForbiddenException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.persistence.category.AdminCategoryCreateRepository;
import ee.toolrental.persistence.category.Category;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataAccessResourceFailureException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminCategoryCreateServiceTest {

    private final AdminCategoryCreateRepository adminCategoryCreateRepository = mock(AdminCategoryCreateRepository.class);
    private final AdminCategoryCreateService adminCategoryCreateService =
            new AdminCategoryCreateService(adminCategoryCreateRepository);

    /**
     * Uus nimi: salvestatakse kategooria täpselt päringu nime, kirjelduse ja järjekorraga (saveAndFlush).
     */
    @Test
    void newName_savesCategoryWithRequestValues() {
        when(adminCategoryCreateRepository.existsCategoryNamed("Talvetööd")).thenReturn(false);

        adminCategoryCreateService.createCategory(
                new CategoryCreateRequestDto("Talvetööd", "Lumelabidad, jääpurustajad", 400));

        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(adminCategoryCreateRepository).saveAndFlush(categoryCaptor.capture());
        assertEquals("Talvetööd", categoryCaptor.getValue().getCategoryName());
        assertEquals("Lumelabidad, jääpurustajad", categoryCaptor.getValue().getDescription());
        assertEquals(400, categoryCaptor.getValue().getSequence());
    }

    /**
     * Puuduv kirjeldus (null) on lubatud ja salvestatakse nullina.
     */
    @Test
    void nullDescription_isSavedAsNull() {
        adminCategoryCreateService.createCategory(new CategoryCreateRequestDto("Talvetööd", null, 400));

        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(adminCategoryCreateRepository).saveAndFlush(categoryCaptor.capture());
        assertNull(categoryCaptor.getValue().getDescription());
    }

    /**
     * Olemasolev nimi annab 403 CATEGORY_UNAVAILABLE ja midagi ei salvestata.
     */
    @Test
    void existingName_producesCategoryUnavailableAndSavesNothing() {
        when(adminCategoryCreateRepository.existsCategoryNamed("Aiatööd")).thenReturn(true);

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> adminCategoryCreateService.createCategory(new CategoryCreateRequestDto("Aiatööd", null, 100)));

        assertEquals("CATEGORY_UNAVAILABLE", exception.getErrorCode());
        assertEquals("Sellise nimega kategooria on juba olemas", exception.getMessage());
        verify(adminCategoryCreateRepository, never()).saveAndFlush(any());
    }

    /**
     * Samaaegne duplikaat: kontroll läbib, aga andmebaasi UNIQUE piirang lükkab salvestamise tagasi.
     * Tulemus on sama 403 CATEGORY_UNAVAILABLE, mitte 500.
     */
    @Test
    void concurrentDuplicate_producesCategoryUnavailable() {
        when(adminCategoryCreateRepository.existsCategoryNamed("Talvetööd")).thenReturn(false);
        when(adminCategoryCreateRepository.saveAndFlush(any()))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> adminCategoryCreateService.createCategory(new CategoryCreateRequestDto("Talvetööd", null, 400)));

        assertEquals("CATEGORY_UNAVAILABLE", exception.getErrorCode());
    }

    /**
     * Muu andmebaasi tõrge muutub 500 InternalServerErrorException'iks kokkulepitud sõnumiga, SQL-i kliendile ei näidata.
     */
    @Test
    void databaseFailure_producesInternalServerErrorWithoutTechnicalDetails() {
        when(adminCategoryCreateRepository.existsCategoryNamed("Talvetööd"))
                .thenThrow(new DataAccessResourceFailureException("SQL details"));

        InternalServerErrorException exception = assertThrows(InternalServerErrorException.class,
                () -> adminCategoryCreateService.createCategory(new CategoryCreateRequestDto("Talvetööd", null, 400)));

        assertEquals("INTERNAL_SERVER_ERROR", exception.getErrorCode());
        assertEquals("Kategooria lisamine ebaõnnestus. Palun proovi hiljem uuesti.", exception.getMessage());
        assertFalse(exception.getMessage().contains("SQL details"));
    }
}
