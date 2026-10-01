package ee.toolrental.controller.admin;

import ee.toolrental.controller.admin.dto.AdminCategoryDto;
import ee.toolrental.infrastructure.error.ApiError;
import ee.toolrental.service.AdminCategoryListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AdminCategoryListController {
    private final AdminCategoryListService adminCategoryListService;

    /**
     * Tagastab AdminView kategooriate tabeli jaoks kõik kategooriad (id, nimi, kirjeldus, järjekorranumber).
     * Ligipääs on ainult adminile: reegel /api/admin/** on SecurityConfig'is (401 ja 403 on tühja body'ga).
     */
    @GetMapping("/admin/categories")
    @Operation(summary = "Admini kategooriate nimekirja päring",
            description = "Tagastab kõik kategooriad järjestuses sequence ASC; erinevalt avalikust GET /api/categories päringust sisaldab description ja sequence välju.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Kasutaja pole sisse logitud"),
            @ApiResponse(responseCode = "403", description = "Kasutaja roll pole admin"),
            @ApiResponse(responseCode = "500", description = "Kategooriate laadimine ebaõnnestus (INTERNAL_SERVER_ERROR)",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public List<AdminCategoryDto> getCategories() {
        return adminCategoryListService.getCategories();
    }
}
