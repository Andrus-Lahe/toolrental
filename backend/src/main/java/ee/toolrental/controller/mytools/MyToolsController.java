package ee.toolrental.controller.mytools;

import ee.toolrental.controller.mytools.dto.MyToolsResponseDto;
import ee.toolrental.infrastructure.security.AppUserPrincipal;
import ee.toolrental.service.MyToolsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me/tools")
@RequiredArgsConstructor
public class MyToolsController {
    private final MyToolsService myToolsService;

    @GetMapping
    @Operation(summary = "Minu tööriistade ja laenutuste ülevaade")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Minu tööriistade ülevaade"),
            @ApiResponse(responseCode = "401", description = "Vaate avamiseks logi sisse"),
            @ApiResponse(responseCode = "403", description = "Kasutaja konto on blokeeritud"),
            @ApiResponse(responseCode = "404", description = "Kasutaja profiili ei leitud"),
            @ApiResponse(responseCode = "500", description = "Minu tööriistade laadimine ebaõnnestus")})
    public MyToolsResponseDto getMyTools(@AuthenticationPrincipal AppUserPrincipal principal) {
        return myToolsService.getMyTools(principal.getUserId());
    }
}
