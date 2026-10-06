package ee.toolrental.aimodule;

public record AvailableTool(
        Integer toolId,
        String name,
        String category,
        String city,
        String district
) {
}
