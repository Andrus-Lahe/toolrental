package ee.toolrental.controller.admin;

import ee.toolrental.controller.admin.dto.CategoryUpdateRequestDto;
import ee.toolrental.infrastructure.error.ApiError;
import ee.toolrental.service.AdminCategoryUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AdminCategoryUpdateController {
    private final AdminCategoryUpdateService adminCategoryUpdateService;

    /**
     * Muudab valitud kategooria nime, kirjelduse ja järjekorranumbri ning vastab 200 tühja body'ga.
     * Ligipääs on ainult adminile: reegel /api/admin/** on SecurityConfig'is (401 ja 403 on tühja body'ga).
     */
    @PutMapping("/admin/categories/{categoryId}")
    @Operation(summary = "Admini kategooria muutmine",
            description = "Asendab kategooria nime, kirjelduse ja järjekorranumbri. Sama kategooria enda nime uuesti salvestamine on lubatud; pilt ja tööriistad ei muutu.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Vigane sisend (INCORRECT_INPUT)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Kasutaja pole sisse logitud"),
            @ApiResponse(responseCode = "403", description = "Roll pole admin (tühi body) või CATEGORY_UNAVAILABLE",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Kategooriat ei leitud (PRIMARY_KEY_NOT_FOUND)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Kategooria muutmine ebaõnnestus (INTERNAL_SERVER_ERROR)",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public void updateCategory(@PathVariable Integer categoryId,
                               @RequestBody @Valid CategoryUpdateRequestDto categoryUpdateRequestDto) {
        adminCategoryUpdateService.updateCategory(categoryId, categoryUpdateRequestDto);
    }
}
