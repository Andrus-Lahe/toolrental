package ee.toolrental.service;

public record ToolSearchIntent(
        boolean toolSearch,
        String city,
        String district,
        String category
) {
}
