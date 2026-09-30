package ee.toolrental.service;

import ee.toolrental.controller.category.dto.CategoryDetailedInfoDto;
import ee.toolrental.controller.category.dto.CategoryDto;
import ee.toolrental.infrastructure.exception.CategoryLoadingException;
import ee.toolrental.persistence.category.Category;
import ee.toolrental.persistence.category.CategoryMapper;
import ee.toolrental.persistence.category.CategoryRepository;
import ee.toolrental.persistence.categoryimage.CategoryImage;
import ee.toolrental.persistence.categoryimage.CategoryImageRepository;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CategoryServiceTest {

    private final CategoryRepository categoryRepository = mock(CategoryRepository.class);
    private final CategoryImageRepository categoryImageRepository = mock(CategoryImageRepository.class);
    private final CategoryMapper categoryMapper = Mappers.getMapper(CategoryMapper.class);
    private final CategoryService categoryService = new CategoryService(
            categoryRepository, categoryMapper, categoryImageRepository);

    @Test
    void categoryListContainsOnlyIdAndNameInRepositoryOrder() {
        when(categoryRepository.findAllCategories()).thenReturn(List.of(
                category(3, "Koristamine", "Kirjeldus"),
                category(1, "Aiatööd", null)));

        List<CategoryDto> result = categoryService.getCategories();

        assertEquals(List.of(new CategoryDto(3, "Koristamine"), new CategoryDto(1, "Aiatööd")), result);
    }

    @Test
    void emptyCategoryTableProducesEmptyCategoryList() {
        when(categoryRepository.findAllCategories()).thenReturn(List.of());

        assertTrue(categoryService.getCategories().isEmpty());
    }

    @Test
    void categoryListRepositoryFailureProducesCategoryLoadingErrorWithoutTechnicalDetails() {
        when(categoryRepository.findAllCategories()).thenThrow(new IllegalStateException("SQL details"));

        CategoryLoadingException exception = assertThrows(
                CategoryLoadingException.class, categoryService::getCategories);

        assertEquals("INTERNAL_SERVER_ERROR", exception.getErrorCode());
        assertEquals("Kategooriate laadimine ebaõnnestus. Palun proovi hiljem uuesti.", exception.getMessage());
        assertFalse(exception.getMessage().contains("SQL details"));
    }

    @Test
    void categoryWithoutImageRemainsInResult() {
        Category category = category(7, "Pildita", null);
        when(categoryRepository.findAllCategories()).thenReturn(List.of(category));
        when(categoryImageRepository.findAllCategoriesImages()).thenReturn(List.of());

        List<CategoryDetailedInfoDto> result = categoryService.getCategoriesInfo();

        assertEquals(1, result.size());
        assertEquals(7, result.getFirst().getCategoryId());
        assertEquals("Pildita", result.getFirst().getCategoryName());
        assertNull(result.getFirst().getCategoryDescription());
        assertNull(result.getFirst().getImageData());
    }

    @Test
    void imageDataIsBase64OfOriginalBytes() {
        Category category = category(3, "Pildiga", "Kirjeldus");
        byte[] imageBytes = "<svg>Õ</svg>".getBytes(StandardCharsets.UTF_8);
        CategoryImage image = new CategoryImage();
        image.setCategory(category);
        image.setImageData(imageBytes);
        when(categoryRepository.findAllCategories()).thenReturn(List.of(category));
        when(categoryImageRepository.findAllCategoriesImages()).thenReturn(List.of(image));

        CategoryDetailedInfoDto result = categoryService.getCategoriesInfo().getFirst();

        assertEquals("Kirjeldus", result.getCategoryDescription());
        assertArrayEquals(imageBytes, Base64.getDecoder().decode(result.getImageData()));
        assertFalse(result.getImageData().startsWith("data:"));
    }

    @Test
    void emptyCategoryTableProducesEmptyList() {
        when(categoryRepository.findAllCategories()).thenReturn(List.of());
        when(categoryImageRepository.findAllCategoriesImages()).thenReturn(List.of());

        assertTrue(categoryService.getCategoriesInfo().isEmpty());
    }

    @Test
    void repositoryFailureProducesCategoryLoadingErrorWithoutTechnicalDetails() {
        when(categoryRepository.findAllCategories()).thenThrow(new IllegalStateException("SQL details"));

        CategoryLoadingException exception = assertThrows(
                CategoryLoadingException.class, categoryService::getCategoriesInfo);

        assertEquals("INTERNAL_SERVER_ERROR", exception.getErrorCode());
        assertEquals("Kategooriate laadimine ebaõnnestus. Palun proovi hiljem uuesti.", exception.getMessage());
        assertFalse(exception.getMessage().contains("SQL details"));
    }

    private Category category(int id, String name, String description) {
        Category category = new Category();
        category.setId(id);
        category.setCategoryName(name);
        category.setDescription(description);
        return category;
    }
}
