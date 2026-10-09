package ee.toolrental.controller.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ToolAskRequest(
        @NotBlank(message = "ei tohi olla tühi")
        @Size(max = 500, message = "ei tohi olla pikem kui 500 märki")
        String question
) {
}
