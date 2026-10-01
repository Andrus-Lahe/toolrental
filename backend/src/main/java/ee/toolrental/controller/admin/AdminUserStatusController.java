package ee.toolrental.controller.admin;

import ee.toolrental.controller.admin.dto.UserStatusRequestDto;
import ee.toolrental.infrastructure.error.ApiError;
import ee.toolrental.infrastructure.security.AppUserPrincipal;
import ee.toolrental.service.AdminUserStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AdminUserStatusController {
    private final AdminUserStatusService adminUserStatusService;

    /**
     * Muudab valitud kasutaja oleku (B = blokeeri, A = aktiivne) ja vastab 200 tühja body'ga.
     * Admini enda ID võetakse sessioonist; ligipääs on ainult adminile (reegel /api/admin/** SecurityConfig'is).
     */
    @PatchMapping("/admin/users/{userId}/status")
    @Operation(summary = "Admini kasutaja oleku muutmine (blokeerimine)",
            description = "Seab app_user.status väärtuseks A või B. Sama oleku uuesti saatmine annab samuti 200. Iseennast ei saa blokeerida.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Vigane või puuduv status (INCORRECT_INPUT)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Kasutaja pole sisse logitud"),
            @ApiResponse(responseCode = "403", description = "Roll pole admin (tühi body) või SELF_BLOCK_NOT_ALLOWED",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Kasutajat ei leitud (PRIMARY_KEY_NOT_FOUND)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Oleku muutmine ebaõnnestus (INTERNAL_SERVER_ERROR)",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public void changeUserStatus(@AuthenticationPrincipal AppUserPrincipal principal,
                                 @PathVariable Integer userId,
                                 @RequestBody @Valid UserStatusRequestDto userStatusRequestDto) {
        adminUserStatusService.changeUserStatus(principal.getUserId(), userId, userStatusRequestDto);
    }
}
