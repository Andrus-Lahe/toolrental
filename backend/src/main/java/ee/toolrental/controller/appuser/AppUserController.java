package ee.toolrental.controller.appuser;

import ee.toolrental.controller.appuser.dto.CurrentUserDto;
import ee.toolrental.infrastructure.security.AppUserPrincipal;
import ee.toolrental.service.AppUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AppUserController {
    private final AppUserService appUserService;

    @GetMapping("/me")
    @Operation(summary = "Sisselogitud kasutaja profiili olemasolu kontroll")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Kasutaja pole sisse logitud")})
    public CurrentUserDto getCurrentUser(@AuthenticationPrincipal AppUserPrincipal principal) {
        return appUserService.getCurrentUser(principal.getUserId(), principal.getEmail());

    }
}
