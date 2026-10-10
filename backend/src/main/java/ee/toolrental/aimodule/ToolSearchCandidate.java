package ee.toolrental.aimodule;

public record ToolSearchCandidate(Integer toolId, String name, String description,
                                  String category, String location, boolean availableNow) {
    AvailableTool toAvailableTool() {
        return new AvailableTool(toolId, name, category, location);
    }
}
