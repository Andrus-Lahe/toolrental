package ee.toolrental.controller.admin;

import ee.toolrental.controller.admin.dto.AdminUserDto;
import ee.toolrental.infrastructure.error.ApiError;
import ee.toolrental.service.AdminUserListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AdminUserListController {
    private final AdminUserListService adminUserListService;

    /**
     * Tagastab AdminView kasutajate tabeli jaoks kõik süsteemi kasutajad (sh blokeeritud ja profiilita).
     * Ligipääs on ainult adminile: reegel /api/admin/** on SecurityConfig'is (401 ja 403 on tühja body'ga).
     */
    @GetMapping("/admin/users")
    @Operation(summary = "Admini kasutajate nimekirja päring",
            description = "Tagastab kõik kasutajad järjestuses app_user.id ASC; profiilita kasutajal on email ja registeredAt null.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Kasutaja pole sisse logitud"),
            @ApiResponse(responseCode = "403", description = "Kasutaja roll pole admin"),
            @ApiResponse(responseCode = "500", description = "Kasutajate laadimine ebaõnnestus (INTERNAL_SERVER_ERROR)",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public List<AdminUserDto> getUsers() {
        return adminUserListService.getUsers();
    }
}
