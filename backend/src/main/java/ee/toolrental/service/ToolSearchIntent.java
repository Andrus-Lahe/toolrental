package ee.toolrental.service;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

// reasoning on esimene, et mudel kirjutaks põhjenduse enne otsust (chain of thought).
@JsonPropertyOrder({"reasoning", "toolSearch", "city", "district", "category", "toolName"})
public record ToolSearchIntent(
        String reasoning,
        boolean toolSearch,
        String city,
        String district,
        String category,
        String toolName
) {
}
