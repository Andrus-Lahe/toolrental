package ee.toolrental.controller.category;

import ee.toolrental.controller.category.dto.CategoryDetailedInfoDto;
import ee.toolrental.controller.category.dto.CategoryDto;
import ee.toolrental.infrastructure.RestExceptionHandler;
import ee.toolrental.infrastructure.exception.CategoryLoadingException;
import ee.toolrental.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoryControllerTest {

    private final CategoryService categoryService = mock(CategoryService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CategoryController(categoryService))
                .setControllerAdvice(new RestExceptionHandler())
                .build();
    }

    @Test
    void returnsCategoriesWithExpectedShapeAndServiceOrder() throws Exception {
        when(categoryService.getCategories()).thenReturn(List.of(
                new CategoryDto(1, "Aiatööd"),
                new CategoryDto(2, "Ehitustööd"),
                new CategoryDto(3, "Koristamine"),
                new CategoryDto(4, "Muud")));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].categoryId").value(1))
                .andExpect(jsonPath("$[0].categoryName").value("Aiatööd"))
                .andExpect(jsonPath("$[0].length()").value(2))
                .andExpect(jsonPath("$[3].categoryId").value(4))
                .andExpect(jsonPath("$[3].categoryName").value("Muud"));
    }

    @Test
    void emptyCategoryListProducesOkWithEmptyArray() throws Exception {
        when(categoryService.getCategories()).thenReturn(List.of());

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void categoryListLoadingFailureProducesSpecifiedApiError() throws Exception {
        when(categoryService.getCategories()).thenThrow(new CategoryLoadingException(
                "Kategooriate laadimine ebaõnnestus. Palun proovi hiljem uuesti.",
                "INTERNAL_SERVER_ERROR"));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value(
                        "Kategooriate laadimine ebaõnnestus. Palun proovi hiljem uuesti."));
    }

    @Test
    void returnsCategoryDetailsWithExpectedShape() throws Exception {
        CategoryDetailedInfoDto category = new CategoryDetailedInfoDto(
                1, "Aiatööd", "Muruniidukid", "PHN2Zz48L3N2Zz4=");
        when(categoryService.getCategoriesInfo()).thenReturn(List.of(category));

        mockMvc.perform(get("/api/categories/detailed-info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryId").value(1))
                .andExpect(jsonPath("$[0].categoryName").value("Aiatööd"))
                .andExpect(jsonPath("$[0].categoryDescription").value("Muruniidukid"))
                .andExpect(jsonPath("$[0].imageData").value("PHN2Zz48L3N2Zz4="))
                .andExpect(jsonPath("$[0].length()").value(4));
    }

    @Test
    void emptyCategoriesProduceOkWithEmptyArray() throws Exception {
        when(categoryService.getCategoriesInfo()).thenReturn(List.of());

        mockMvc.perform(get("/api/categories/detailed-info"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void categoryLoadingFailureProducesSpecifiedApiError() throws Exception {
        when(categoryService.getCategoriesInfo()).thenThrow(new CategoryLoadingException(
                "Kategooriate laadimine ebaõnnestus. Palun proovi hiljem uuesti.",
                "INTERNAL_SERVER_ERROR"));

        mockMvc.perform(get("/api/categories/detailed-info"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value(
                        "Kategooriate laadimine ebaõnnestus. Palun proovi hiljem uuesti."));
    }
}
