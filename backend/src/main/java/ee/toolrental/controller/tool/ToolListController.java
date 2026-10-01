package ee.toolrental.controller.tool;

import ee.toolrental.controller.tool.dto.ToolsResponse;
import ee.toolrental.infrastructure.error.ApiError;
import ee.toolrental.service.ToolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ToolListController {
    private final ToolService toolService;

    /**
     * Parameetrid võetakse vastu String-ina, et eristada puuduvat parameetrit (vaikeväärtus)
     * selgelt saadetud tühjast väärtusest (400). Parsimine ja valideerimine toimub ToolService-s.
     */
    @GetMapping("/tools")
    @Operation(summary = "Leiab tööriistade nimekirja filtrite ja lehekülgjaotusega",
            description = "Filtrid ühendatakse AND-tingimusega, tulemused järjestuses tool.id ASC. Lehenumber algab ühest.")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Vigane päringuparameeter (INCORRECT_INPUT)",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "500", description = "Tööriistade laadimine ebaõnnestus (INTERNAL_SERVER_ERROR)",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ToolsResponse getTools(
            @Parameter(description = "Kategooria ID, 0 = kõik (vaikimisi 0)", example = "0")
            @RequestParam(required = false) String categoryId,
            @Parameter(description = "Linna ID, 0 = kõik (vaikimisi 0)", example = "0")
            @RequestParam(required = false) String cityId,
            @Parameter(description = "Linnaosa ID, 0 = kõik (vaikimisi 0)", example = "0")
            @RequestParam(required = false) String districtId,
            @Parameter(description = "A = saadaval, U = pole saadaval, 0 = kõik (vaikimisi A)", example = "A")
            @RequestParam(required = false) String status,
            @Parameter(description = "Lehenumber alates 1 (vaikimisi 1)", example = "1")
            @RequestParam(required = false) String pageNumber,
            @Parameter(description = "Kirjete arv lehel, vähemalt 1 (vaikimisi 12)", example = "12")
            @RequestParam(required = false) String pageSize) {
        return toolService.getTools(categoryId, cityId, districtId, status, pageNumber, pageSize);
    }
}
