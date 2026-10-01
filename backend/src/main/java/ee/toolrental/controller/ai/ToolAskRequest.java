package ee.toolrental.controller.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ToolAskRequest(
        @NotBlank @Size(max = 500) String question
) {
}
