package ee.toolrental.controller.admin;

import ee.toolrental.controller.admin.dto.CategoryCreateRequestDto;
import ee.toolrental.infrastructure.error.ApiError;
import ee.toolrental.service.AdminCategoryCreateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AdminCategoryCreateController {
    private final AdminCategoryCreateService adminCategoryCreateService;

    /**
     * Lisab uue kategooria (nimi, kirjeldus, järjekorranumber) ja vastab 200 tühja body'ga.
     * Ligipääs on ainult adminile: reegel /api/admin/** on SecurityConfig'is (401 ja 403 on tühja body'ga).
     */
    @PostMapping("/admin/categories")
    @Operation(summary = "Admini kategooria lisamine",
            description = "Lisab uue kategooria. Sama nimega kategooriat teist korda lisada ei saa; pilti selles versioonis ei lisata.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Vigane sisend (INCORRECT_INPUT)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Kasutaja pole sisse logitud"),
            @ApiResponse(responseCode = "403", description = "Roll pole admin (tühi body) või CATEGORY_UNAVAILABLE",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Kategooria lisamine ebaõnnestus (INTERNAL_SERVER_ERROR)",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public void createCategory(@RequestBody @Valid CategoryCreateRequestDto categoryCreateRequestDto) {
        adminCategoryCreateService.createCategory(categoryCreateRequestDto);
    }
}
