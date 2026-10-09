package ee.toolrental.aimodule;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ToolSearchSqlValidatorTest {

    private final ToolSearchSqlValidator validator = new ToolSearchSqlValidator();

    @Test
    void acceptsGeneratedQueryForRestrictedView() {
        String sql = "SELECT tool_id, tool_name, category_name, location_name " +
                "FROM tool_rental.ai_tool_search WHERE is_available_now = TRUE " +
                "AND location_name ILIKE '%Kristiine%' ORDER BY tool_id LIMIT 20;";

        assertThat(validator.validate(sql).value()).isEqualTo(sql.trim());
    }

    @Test
    void extractsSafeFiltersIncludingDescriptionOrGroups() {
        String sql = "SELECT tool_id, tool_name, category_name, location_name " +
                "FROM tool_rental.ai_tool_search WHERE is_available_now = TRUE " +
                "AND (tool_name ILIKE '%labidas%' OR tool_description ILIKE '%labidas%') " +
                "AND location_name ILIKE '%Nõmme%' ORDER BY tool_id LIMIT 20";

        var filters = validator.validate(sql).filters();

        assertThat(filters).hasSize(2);
        assertThat(filters.getFirst().alternatives())
                .extracting(ToolSearchSqlValidator.FilterCondition::column)
                .containsExactly("tool_name", "tool_description");
        assertThat(filters.getFirst().alternatives())
                .extracting(ToolSearchSqlValidator.FilterCondition::value)
                .containsExactly("%labidas%", "%labidas%");
        assertThat(filters.get(1).alternatives().getFirst().column()).isEqualTo("location_name");
    }

    @Test
    void rejectsNonSelectAndMultipleStatements() {
        assertRejected("DELETE FROM tool_rental.ai_tool_search");
        assertRejected(validQuery() + "; SELECT 1");
    }

    @Test
    void rejectsOtherRelationsAndSubqueries() {
        assertRejected(validQuery().replace("tool_rental.ai_tool_search", "tool_rental.tool"));
        assertRejected(validQuery().replace("location_name ILIKE '%Kristiine%'",
                "location_name ILIKE (SELECT email FROM tool_rental.profile)"));
    }

    @Test
    void rejectsMissingAvailabilityAndWrongProjection() {
        assertRejected(validQuery().replace("is_available_now = TRUE AND ", ""));
        assertRejected(validQuery().replace("tool_id, tool_name, category_name, location_name", "tool_id, *"));
    }

    @Test
    void rejectsOrCommentsFunctionsAndLimitOverTwenty() {
        assertRejected(validQuery().replace(" AND location_name", " OR location_name"));
        assertRejected(validQuery() + " -- ignored suffix");
        assertRejected(validQuery().replace("location_name ILIKE '%Kristiine%'", "lower(location_name) = 'x'"));
        assertRejected(validQuery().replace("LIMIT 20", "LIMIT 21"));
    }

    private String validQuery() {
        return "SELECT tool_id, tool_name, category_name, location_name " +
                "FROM tool_rental.ai_tool_search WHERE is_available_now = TRUE " +
                "AND location_name ILIKE '%Kristiine%' ORDER BY tool_id LIMIT 20";
    }

    private void assertRejected(String sql) {
        assertThatThrownBy(() -> validator.validate(sql))
                .isInstanceOf(InvalidToolSearchSqlException.class)
                .hasMessage("Generated SQL is outside the allowed tool search query contract");
    }
}
