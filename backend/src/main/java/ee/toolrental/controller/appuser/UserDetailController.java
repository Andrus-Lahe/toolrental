package ee.toolrental.controller.appuser;

import ee.toolrental.controller.appuser.dto.UserDetailResponse;
import ee.toolrental.infrastructure.error.ApiError;
import ee.toolrental.service.UserDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserDetailController {

    private final UserDetailService userDetailService;

    @GetMapping("/{userId}")
    @Operation(summary = "Kasutaja kontaktandmete päring")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Kasutaja kontaktandmed"),
            @ApiResponse(responseCode = "400", description = "userId peab olema täisarv",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Kasutaja pole sisse logitud"),
            @ApiResponse(responseCode = "404", description = "Kasutajat ei leitud",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Kasutaja andmete laadimine ebaõnnestus",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public UserDetailResponse getUserDetails(@PathVariable Integer userId) {
        return userDetailService.getUserDetails(userId);
    }
}
