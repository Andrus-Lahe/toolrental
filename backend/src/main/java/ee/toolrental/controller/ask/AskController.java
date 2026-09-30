package ee.toolrental.controller.ask;

import ee.toolrental.controller.ask.dto.AskRequest;
import ee.toolrental.controller.ask.dto.AskResponse;
import ee.toolrental.infrastructure.error.ApiError;
import ee.toolrental.service.NlToSqlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AskController {
    private final NlToSqlService nlToSqlService;

    // REST teenus POST /api/admin/ask: võtab päringu kehast admini küsimuse tavakeeles.
    // @Valid kontrollib enne, et küsimus poleks tühi ega liiga pikk; seejärel annab küsimuse
    // NlToSqlService'ile ja tagastab AI koostatud vastuse JSON-ina.
    @PostMapping("/admin/ask")
    @Operation(summary = "Küsimus andmebaasi kohta tavakeeles (AI koostab SQL päringu ja võtab tulemuse kokku)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Vigane sisend (INCORRECT_INPUT)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Kasutaja pole sisse logitud"),
            @ApiResponse(responseCode = "403", description = "Kasutaja pole admin või AI koostas keelatud päringu (SQL_NOT_ALLOWED)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "AI mudel või päringu käivitamine ebaõnnestus",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public AskResponse ask(@RequestBody @Valid AskRequest askRequest) {
        return nlToSqlService.ask(askRequest.getQuestion());
    }
}
