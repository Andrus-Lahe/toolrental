package ee.toolrental.aimodule;

import java.util.List;

public record ToolAskResponse(String answer, List<AvailableTool> tools) {
    public ToolAskResponse {
        tools = List.copyOf(tools);
    }
}
