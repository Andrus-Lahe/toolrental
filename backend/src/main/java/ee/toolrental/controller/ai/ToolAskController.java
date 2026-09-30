package ee.toolrental.controller.ai;

import ee.toolrental.service.ToolAskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/ask")
@RequiredArgsConstructor
public class ToolAskController {

    private final ToolAskService toolAskService;

    @PostMapping
    @Operation(summary = "Saadaval tööriistade otsimine loomuliku keele küsimusega")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Otsingu vastus"),
            @ApiResponse(responseCode = "400", description = "Vigane küsimus"),
            @ApiResponse(responseCode = "401", description = "Sisselogimine on vajalik")
    })
    public ToolAskResponse ask(@Valid @RequestBody ToolAskRequest request) {
        return toolAskService.ask(request.question());
    }
}
