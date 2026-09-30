package ee.toolrental.controller.category;
import ee.toolrental.controller.category.dto.CategoryDetailedInfoDto;
import ee.toolrental.controller.category.dto.CategoryDto;
import ee.toolrental.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/categories")
    @Operation(summary = "Leiab süsteemist kõik kategooriad (otsingufiltri lihtloend)")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "500", description = "Kategooriate laadimine ebaõnnestus")
    })
    public List<CategoryDto> getCategories() {
        return categoryService.getCategories();
    }

    @GetMapping("/categories/detailed-info")
    @Operation(summary = "Kategooriate detailinfo päring")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "500", description = "Kategooriate laadimine ebaõnnestus")
    })
    public List<CategoryDetailedInfoDto> getCategoriesInfo() {
       return categoryService.getCategoriesInfo();
    }
}
