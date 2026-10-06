package ee.toolrental.aimodule;

public record ToolSearchIntent(
        boolean toolSearch,
        String city,
        String district,
        String category
) {
}
