package ee.toolrental.controller.profile;

import ee.toolrental.controller.profile.dto.ProfileDto;
import ee.toolrental.controller.profile.dto.ProfileUpdateRequestDto;
import ee.toolrental.infrastructure.error.ApiError;
import ee.toolrental.infrastructure.security.AppUserPrincipal;
import ee.toolrental.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ProfileController {
    private final ProfileService profileService;

    /**
     * Tagastab sisselogitud kasutaja profiili andmed MyProfile vormi jaoks.
     * Kasutaja ID ja Google'i e-post võetakse sessioonist, mitte päringust.
     */
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

    /**
     * Salvestab sisselogitud kasutaja profiili: esmakordsel täitmisel loob, hiljem uuendab.
     * Kasutaja ID võetakse sessioonist; päringu keha valideeritakse enne service'i kutset.
     */
    @PutMapping("/users/me/profile")
    @Operation(summary = "Sisselogitud kasutaja profiili salvestamine",
            description = "Profiili puudumisel luuakse location ja profile rida, olemasolul uuendatakse neid. Kasutaja nimi uuendatakse app_user tabelis.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Vigane sisend (INCORRECT_INPUT)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Kasutaja pole sisse logitud"),
            @ApiResponse(responseCode = "403", description = "Sellise e-postiga kasutaja on juba süsteemis olemas (EMAIL_ALREADY_EXISTS)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Linnaosa ei leitud (PRIMARY_KEY_NOT_FOUND)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Profiili salvestamine ebaõnnestus",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public void updateProfile(@AuthenticationPrincipal AppUserPrincipal principal,
                              @RequestBody @Valid ProfileUpdateRequestDto profileUpdateRequestDto) {
        profileService.updateProfile(principal.getUserId(), profileUpdateRequestDto);
    }
}
