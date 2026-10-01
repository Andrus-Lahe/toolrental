package ee.toolrental.controller.tool;

import ee.toolrental.infrastructure.error.ApiError;
import ee.toolrental.infrastructure.security.AppUserPrincipal;
import ee.toolrental.service.ToolDeleteService;
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
public class ToolDeleteController {
    private final ToolDeleteService toolDeleteService;

    /**
     * Kustutab sisselogitud kasutaja enda tööriista koos piltidega ja vastab 200 tühja body'ga.
     * Kasutaja ID võetakse sessioonist; sisselogimata kasutaja saab 401 (reegel anyRequest().authenticated()).
     */
    @DeleteMapping("/tools/{toolId}")
    @Operation(summary = "Tööriista kustutamine",
            description = "Kustutab tööriista ja selle pildid. Kustutada saab ainult enda tööriista, millel pole broneeringuid.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Kasutaja pole sisse logitud"),
            @ApiResponse(responseCode = "403", description = "TOOL_NOT_OWNED või TOOL_HAS_BOOKINGS",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Tööriista ei leitud (PRIMARY_KEY_NOT_FOUND)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Tööriista kustutamine ebaõnnestus (INTERNAL_SERVER_ERROR)",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public void deleteTool(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable Integer toolId) {
        toolDeleteService.deleteTool(principal.getUserId(), toolId);
    }
}
