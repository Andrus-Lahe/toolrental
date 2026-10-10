package ee.toolrental.aimodule;

import java.util.List;

public record ToolSearchMatchResult(boolean toolSearchQuestion, List<Integer> toolIds) {
    public ToolSearchMatchResult {
        toolIds = List.copyOf(toolIds);
    }
}
