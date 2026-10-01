package ee.toolrental.controller.tool;

import ee.toolrental.controller.tool.dto.ToolCreateRequestDto;
import ee.toolrental.controller.tool.dto.ToolDetailResponse;
import ee.toolrental.infrastructure.security.AppUserPrincipal;
import ee.toolrental.service.ToolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ToolController {
    private final ToolService toolService;

    @PostMapping("/tools")
    @Operation(summary = "Tööriista lisamine")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tööriist lisati"),
            @ApiResponse(responseCode = "400", description = "Vigane sisend"),
            @ApiResponse(responseCode = "401", description = "Kasutaja pole sisse logitud"),
            @ApiResponse(responseCode = "403", description = "Kasutaja profiil on täitmata"),
            @ApiResponse(responseCode = "404", description = "Kategooriat ei leitud"),
            @ApiResponse(responseCode = "500", description = "Tööriista lisamine ebaõnnestus")})
    public void createTool(@AuthenticationPrincipal AppUserPrincipal principal,
                           @RequestBody @Valid ToolCreateRequestDto request) {
        toolService.createTool(principal.getUserId(), request);
    }

    @GetMapping("/tools/{toolId}")
    @Operation(summary = "Tööriista detailide päring",
            description = "Avalik. Tagastab tööriista andmed koos põhipildiga (is_main = true) Base64 kujul.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "toolId pole täisarv (INCORRECT_INPUT)"),
            @ApiResponse(responseCode = "404", description = "Tööriista ei leitud (PRIMARY_KEY_NOT_FOUND)"),
            @ApiResponse(responseCode = "500", description = "Tööriista laadimine ebaõnnestus")})
    public ToolDetailResponse getToolDetail(@PathVariable Integer toolId) {
        return toolService.getToolDetail(toolId);
    }
}
