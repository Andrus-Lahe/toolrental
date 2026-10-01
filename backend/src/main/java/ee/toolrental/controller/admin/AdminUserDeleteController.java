package ee.toolrental.controller.admin;

import ee.toolrental.infrastructure.error.ApiError;
import ee.toolrental.infrastructure.security.AppUserPrincipal;
import ee.toolrental.service.AdminUserDeleteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AdminUserDeleteController {
    private final AdminUserDeleteService adminUserDeleteService;

    /**
     * Kustutab admini valitud kasutaja koos tema profiiliga ja vastab 200 tühja body'ga.
     * Admini enda ID võetakse sessioonist; ligipääs on ainult adminile (reegel /api/admin/** SecurityConfig'is).
     */
    @DeleteMapping("/admin/users/{userId}")
    @Operation(summary = "Admini kasutaja kustutamine",
            description = "Kustutab profiili ja kasutaja. Kasutajat, kellel on tööriistu või broneeringuid, ning iseennast kustutada ei saa.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Kasutaja pole sisse logitud"),
            @ApiResponse(responseCode = "403", description = "Roll pole admin (tühi body) või SELF_DELETE_NOT_ALLOWED / USER_HAS_DATA",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Kasutajat ei leitud (PRIMARY_KEY_NOT_FOUND)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Kasutaja kustutamine ebaõnnestus (INTERNAL_SERVER_ERROR)",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public void deleteUser(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable Integer userId) {
        adminUserDeleteService.deleteUser(principal.getUserId(), userId);
    }
}
