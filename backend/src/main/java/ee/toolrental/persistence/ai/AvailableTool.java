package ee.toolrental.persistence.ai;

public record AvailableTool(
        Integer toolId,
        String name,
        String category,
        String city,
        String district
) {
}
