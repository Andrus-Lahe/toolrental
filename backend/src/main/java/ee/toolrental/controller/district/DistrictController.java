package ee.toolrental.controller.district;

import ee.toolrental.controller.district.dto.DistrictDto;
import ee.toolrental.infrastructure.error.ApiError;
import ee.toolrental.service.DistrictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DistrictController {
    private final DistrictService districtService;

    @GetMapping("/cities/{cityId}/districts")
    @Operation(summary = "Leiab valitud linna linnaosad",
            description = "Tagastab ainult valitud linna linnaosad järjestuses district.id ASC.")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Vigane cityId (INCORRECT_INPUT)",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Linna ei leitud (PRIMARY_KEY_NOT_FOUND)",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "500", description = "Linnaosade laadimine ebaõnnestus (INTERNAL_SERVER_ERROR)",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public List<DistrictDto> getDistricts(@PathVariable Integer cityId) {
        return districtService.getDistricts(cityId);
    }

}
