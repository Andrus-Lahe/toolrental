package ee.toolrental.controller.category;

import ee.toolrental.infrastructure.security.DevSecurityConfig;
import ee.toolrental.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@Import(DevSecurityConfig.class)
@ActiveProfiles("dev-no-auth")
@EnableWebSecurity
class CategoryPublicAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void guestCanReadCategoryList() throws Exception {
        when(categoryService.getCategories()).thenReturn(List.of());

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void guestCanReadCategoryDetails() throws Exception {
        when(categoryService.getCategoriesInfo()).thenReturn(List.of());

        mockMvc.perform(get("/api/categories/detailed-info"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
