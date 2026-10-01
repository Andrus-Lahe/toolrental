package ee.toolrental.controller.admin;

import ee.toolrental.infrastructure.error.ApiError;
import ee.toolrental.service.AdminCategoryDeleteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AdminCategoryDeleteController {
    private final AdminCategoryDeleteService adminCategoryDeleteService;

    /**
     * Kustutab valitud kategooria koos pildiga ja vastab 200 tühja body'ga.
     * Ligipääs on ainult adminile: reegel /api/admin/** on SecurityConfig'is (401 ja 403 on tühja body'ga).
     */
    @DeleteMapping("/admin/categories/{categoryId}")
    @Operation(summary = "Admini kategooria kustutamine",
            description = "Kustutab kategooria pildi ja kategooria. Kategooriat, milles on tööriistu, kustutada ei saa.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Kasutaja pole sisse logitud"),
            @ApiResponse(responseCode = "403", description = "Roll pole admin (tühi body) või CATEGORY_IN_USE",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Kategooriat ei leitud (PRIMARY_KEY_NOT_FOUND)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Kategooria kustutamine ebaõnnestus (INTERNAL_SERVER_ERROR)",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public void deleteCategory(@PathVariable Integer categoryId) {
        adminCategoryDeleteService.deleteCategory(categoryId);
    }
}
