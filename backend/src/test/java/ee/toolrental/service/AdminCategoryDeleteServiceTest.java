package ee.toolrental.service;

import ee.toolrental.infrastructure.exception.ForbiddenException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.toolrental.persistence.category.AdminCategoryDeleteRepository;
import ee.toolrental.persistence.category.Category;
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

class AdminCategoryDeleteServiceTest {

    private final AdminCategoryDeleteRepository adminCategoryDeleteRepository = mock(AdminCategoryDeleteRepository.class);
    private final CategoryService categoryService = mock(CategoryService.class);
    private final AdminCategoryDeleteService adminCategoryDeleteService =
            new AdminCategoryDeleteService(adminCategoryDeleteRepository, categoryService);

    /**
     * Tööriistadeta kategooria kustutatakse: kõigepealt pilt, seejärel category rida.
     * Järjekord on oluline, sest category_image.category_id välisvõtmel puudub ON DELETE CASCADE.
     */
    @Test
    void categoryWithoutTools_deletesImageBeforeCategory() {
        when(categoryService.getValidCategoryBy(9)).thenReturn(new Category());
        when(adminCategoryDeleteRepository.deleteCategoryImageOf(9)).thenReturn(1);
        when(adminCategoryDeleteRepository.deleteCategoryBy(9)).thenReturn(1);

        adminCategoryDeleteService.deleteCategory(9);

        InOrder inOrder = inOrder(adminCategoryDeleteRepository);
        inOrder.verify(adminCategoryDeleteRepository).deleteCategoryImageOf(9);
        inOrder.verify(adminCategoryDeleteRepository).deleteCategoryBy(9);
    }

    /**
     * Pildita kategooria kustutamine õnnestub samuti: pildi kustutamine annab 0 rida, kategooria kustutatakse ikkagi.
     */
    @Test
    void categoryWithoutImage_isStillDeleted() {
        when(categoryService.getValidCategoryBy(9)).thenReturn(new Category());
        when(adminCategoryDeleteRepository.deleteCategoryImageOf(9)).thenReturn(0);
        when(adminCategoryDeleteRepository.deleteCategoryBy(9)).thenReturn(1);

        adminCategoryDeleteService.deleteCategory(9);

        verify(adminCategoryDeleteRepository).deleteCategoryBy(9);
    }

    /**
     * Olematu kategooria annab 404 PRIMARY_KEY_NOT_FOUND tegeliku ID-ga ja midagi ei kustutata.
     */
    @Test
    void missingCategory_producesPrimaryKeyNotFoundAndDeletesNothing() {
        when(categoryService.getValidCategoryBy(123)).thenThrow(new PrimaryKeyNotFoundException("categoryId", 123));

        PrimaryKeyNotFoundException exception = assertThrows(
                PrimaryKeyNotFoundException.class, () -> adminCategoryDeleteService.deleteCategory(123));

        assertEquals("Ei leidnud primary keyd 'categoryId' väärtusega: 123", exception.getMessage());
        verifyNoMoreInteractions(adminCategoryDeleteRepository);
    }

    /**
     * Kategooria, milles on tööriistu, annab 403 CATEGORY_IN_USE ja midagi ei kustutata.
     */
    @Test
    void categoryWithTools_producesCategoryInUseAndDeletesNothing() {
        when(categoryService.getValidCategoryBy(4)).thenReturn(new Category());
        when(adminCategoryDeleteRepository.existsToolInCategory(4)).thenReturn(true);

        ForbiddenException exception = assertThrows(
                ForbiddenException.class, () -> adminCategoryDeleteService.deleteCategory(4));

        assertEquals("CATEGORY_IN_USE", exception.getErrorCode());
        assertEquals("Kategooriat ei saa kustutada, sest sellel on tööriistu", exception.getMessage());
        verify(adminCategoryDeleteRepository, never()).deleteCategoryImageOf(4);
        verify(adminCategoryDeleteRepository, never()).deleteCategoryBy(4);
    }

    /**
     * Andmebaasi tõrge muutub 500 InternalServerErrorException'iks kokkulepitud sõnumiga, SQL-i kliendile ei näidata.
     */
    @Test
    void databaseFailure_producesInternalServerErrorWithoutTechnicalDetails() {
        when(categoryService.getValidCategoryBy(9)).thenReturn(new Category());
        when(adminCategoryDeleteRepository.deleteCategoryImageOf(9))
                .thenThrow(new DataAccessResourceFailureException("SQL details"));

        InternalServerErrorException exception = assertThrows(
                InternalServerErrorException.class, () -> adminCategoryDeleteService.deleteCategory(9));

        assertEquals("INTERNAL_SERVER_ERROR", exception.getErrorCode());
        assertEquals("Kategooria kustutamine ebaõnnestus. Palun proovi hiljem uuesti.", exception.getMessage());
        assertFalse(exception.getMessage().contains("SQL details"));
    }
}
