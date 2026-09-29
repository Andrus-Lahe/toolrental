package ee.toolrental.controller.profile;

import ee.toolrental.controller.profile.dto.ProfileDto;
import ee.toolrental.infrastructure.security.AppUserPrincipal;
import ee.toolrental.service.ProfileService;
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
public class ProfileController {
    private final ProfileService profileService;

    @GetMapping("/users/me/profile")
    @Operation(summary = "Sisselogitud kasutaja profiili andmete päring",
            description = "Profiili puudumisel vastatakse samuti 200: hasProfile = false, nimi tuleb app_user tabelist ja e-post Google'i sessioonist.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Kasutaja pole sisse logitud"),
            @ApiResponse(responseCode = "500", description = "Profiili laadimine ebaõnnestus")})
    public ProfileDto getProfile(@AuthenticationPrincipal AppUserPrincipal principal) {
        return profileService.getProfile(principal.getUserId(), principal.getEmail());
    }
}
