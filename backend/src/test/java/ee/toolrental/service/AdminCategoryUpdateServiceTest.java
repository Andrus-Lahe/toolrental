package ee.toolrental.service;

import ee.toolrental.controller.admin.dto.CategoryUpdateRequestDto;
import ee.toolrental.infrastructure.exception.ForbiddenException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.toolrental.persistence.category.AdminCategoryUpdateRepository;
import ee.toolrental.persistence.category.Category;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminCategoryUpdateServiceTest {

    private final AdminCategoryUpdateRepository adminCategoryUpdateRepository = mock(AdminCategoryUpdateRepository.class);
    private final CategoryService categoryService = mock(CategoryService.class);
    private final AdminCategoryUpdateService adminCategoryUpdateService =
            new AdminCategoryUpdateService(adminCategoryUpdateRepository, categoryService);

    /**
     * Kehtiv päring kirjutab kategooria kõik kolm välja üle ja salvestab need kohe (saveAndFlush).
     */
    @Test
    void validRequest_overwritesAllThreeFieldsAndSaves() {
        Category category = category(1, "Aiatööd", "Vana kirjeldus", 100);
        when(categoryService.getValidCategoryBy(1)).thenReturn(category);

        adminCategoryUpdateService.updateCategory(1, new CategoryUpdateRequestDto("Aiatööd2", "Uus kirjeldus", 150));

        assertEquals("Aiatööd2", category.getCategoryName());
        assertEquals("Uus kirjeldus", category.getDescription());
        assertEquals(150, category.getSequence());
        verify(adminCategoryUpdateRepository).saveAndFlush(category);
    }

    /**
     * Null kirjeldus kustutab olemasoleva kirjelduse, sest PUT asendab kogu sisu.
     */
    @Test
    void nullDescription_clearsExistingDescription() {
        Category category = category(1, "Aiatööd", "Vana kirjeldus", 100);
        when(categoryService.getValidCategoryBy(1)).thenReturn(category);

        adminCategoryUpdateService.updateCategory(1, new CategoryUpdateRequestDto("Aiatööd", null, 100));

        assertNull(category.getDescription());
    }

    /**
     * Muutmata nimega salvestamine on lubatud: nime kontroll välistab muudetava kategooria enda (existsOther... on false).
     */
    @Test
    void sameNameOfSameCategory_isAllowed() {
        Category category = category(1, "Aiatööd", "Kirjeldus", 100);
        when(categoryService.getValidCategoryBy(1)).thenReturn(category);
        when(adminCategoryUpdateRepository.existsOtherCategoryNamed("Aiatööd", 1)).thenReturn(false);

        adminCategoryUpdateService.updateCategory(1, new CategoryUpdateRequestDto("Aiatööd", "Kirjeldus", 100));

        verify(adminCategoryUpdateRepository).saveAndFlush(category);
    }

    /**
     * Teise kategooria nimi annab 403 CATEGORY_UNAVAILABLE ja andmed jäävad muutmata.
     */
    @Test
    void nameOfOtherCategory_producesCategoryUnavailableAndKeepsData() {
        Category category = category(1, "Aiatööd", "Kirjeldus", 100);
        when(categoryService.getValidCategoryBy(1)).thenReturn(category);
        when(adminCategoryUpdateRepository.existsOtherCategoryNamed("Ehitustööd", 1)).thenReturn(true);

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> adminCategoryUpdateService.updateCategory(1, new CategoryUpdateRequestDto("Ehitustööd", "Uus", 5)));

        assertEquals("CATEGORY_UNAVAILABLE", exception.getErrorCode());
        assertEquals("Sellise nimega kategooria on juba olemas", exception.getMessage());
        assertEquals("Aiatööd", category.getCategoryName());
        assertEquals(100, category.getSequence());
        verify(adminCategoryUpdateRepository, never()).saveAndFlush(any());
    }

    /**
     * Olematu kategooria annab 404 PRIMARY_KEY_NOT_FOUND tegeliku ID-ga ja nime ei kontrollita ega salvestata.
     */
    @Test
    void missingCategory_producesPrimaryKeyNotFound() {
        when(categoryService.getValidCategoryBy(123)).thenThrow(new PrimaryKeyNotFoundException("categoryId", 123));

        PrimaryKeyNotFoundException exception = assertThrows(PrimaryKeyNotFoundException.class,
                () -> adminCategoryUpdateService.updateCategory(123, new CategoryUpdateRequestDto("Uus", null, 1)));

        assertEquals("Ei leidnud primary keyd 'categoryId' väärtusega: 123", exception.getMessage());
        verify(adminCategoryUpdateRepository, never()).saveAndFlush(any());
    }

    /**
     * Samaaegne duplikaat: kontroll läbib, aga andmebaasi UNIQUE piirang lükkab salvestamise tagasi.
     * Tulemus on sama 403 CATEGORY_UNAVAILABLE, mitte 500.
     */
    @Test
    void concurrentDuplicate_producesCategoryUnavailable() {
        Category category = category(1, "Aiatööd", null, 100);
        when(categoryService.getValidCategoryBy(1)).thenReturn(category);
        when(adminCategoryUpdateRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate key"));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> adminCategoryUpdateService.updateCategory(1, new CategoryUpdateRequestDto("Uus", null, 1)));

        assertEquals("CATEGORY_UNAVAILABLE", exception.getErrorCode());
    }

    /**
     * Muu andmebaasi tõrge muutub 500 InternalServerErrorException'iks kokkulepitud sõnumiga, SQL-i kliendile ei näidata.
     */
    @Test
    void databaseFailure_producesInternalServerErrorWithoutTechnicalDetails() {
        Category category = category(1, "Aiatööd", null, 100);
        when(categoryService.getValidCategoryBy(1)).thenReturn(category);
        when(adminCategoryUpdateRepository.existsOtherCategoryNamed("Uus", 1))
                .thenThrow(new DataAccessResourceFailureException("SQL details"));

        InternalServerErrorException exception = assertThrows(InternalServerErrorException.class,
                () -> adminCategoryUpdateService.updateCategory(1, new CategoryUpdateRequestDto("Uus", null, 1)));

        assertEquals("INTERNAL_SERVER_ERROR", exception.getErrorCode());
        assertEquals("Kategooria muutmine ebaõnnestus. Palun proovi hiljem uuesti.", exception.getMessage());
        assertFalse(exception.getMessage().contains("SQL details"));
    }

    /**
     * Koostab testis kategooria antud väärtustega.
     */
    private Category category(Integer id, String categoryName, String description, Integer sequence) {
        Category category = new Category();
        category.setId(id);
        category.setCategoryName(categoryName);
        category.setDescription(description);
        category.setSequence(sequence);
        return category;
    }
}
