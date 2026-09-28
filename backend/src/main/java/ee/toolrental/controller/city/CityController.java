package ee.toolrental.controller.city;

import ee.toolrental.controller.city.dto.CityDto;
import ee.toolrental.service.CityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CityController {
    private final CityService cityService;

    @GetMapping("/cities")
    @Operation(summary = "Leiab süsteemist kõik linnad")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "500", description = "Internal Server Error")
    public List<CityDto> getCities() {
        return cityService.getCities();
    }

}
