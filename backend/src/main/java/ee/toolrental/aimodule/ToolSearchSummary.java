package ee.toolrental.aimodule;

import java.util.List;

record ToolSearchSummary(List<AvailableTool> tools, long totalToolCount, long availableToolCount,
                         String location, String category) {
    ToolSearchSummary {
        tools = List.copyOf(tools);
    }

    static ToolSearchSummary from(ToolSearchSqlValidator.ValidatedSql query,
                                  ToolSearchQueryRepository.SearchResults results) {
        return new ToolSearchSummary(results.tools(), results.totalToolCount(),
                results.availableToolCount(), uniqueValue(query.filters(), "location_name"),
                uniqueValue(query.filters(), "category_name"));
    }

    ToolSearchAnswerContext answerContext(String question) {
        return new ToolSearchAnswerContext(question, totalToolCount, availableToolCount, location, category);
    }

    private static String uniqueValue(List<ToolSearchSqlValidator.FilterGroup> groups, String column) {
        List<String> values = groups.stream()
                .filter(group -> group.alternatives().size() == 1)
                .map(group -> group.alternatives().getFirst())
                .filter(condition -> condition.column().equals(column)
                        && condition.value() instanceof String)
                .map(condition -> ((String) condition.value()).replaceAll("^%+|%+$", "").trim())
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
        return values.size() == 1 ? values.getFirst() : null;
    }
}
