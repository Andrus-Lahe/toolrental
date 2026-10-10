package ee.toolrental.aimodule;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class ToolSearchQueryRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ToolSearchQueryRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AvailableTool> search(ToolSearchSqlValidator.ValidatedSql query) {
        return jdbcTemplate.query(query.value(), Map.of(), (resultSet, rowNumber) -> new AvailableTool(
                resultSet.getInt("tool_id"),
                resultSet.getString("tool_name"),
                resultSet.getString("category_name"),
                resultSet.getString("location_name")));
    }

    public List<ToolSearchCandidate> findVisibleTools() {
        return jdbcTemplate.query("""
                SELECT tool_id, tool_name, tool_description, category_name, location_name,
                       is_available_now
                FROM tool_rental.ai_tool_search
                WHERE is_visible = TRUE
                ORDER BY tool_id ASC
                """, Map.of(), this::mapCandidate);
    }

    public List<ToolSearchCandidate> findVisibleToolsByExactName(String name) {
        return jdbcTemplate.query("""
                SELECT tool_id, tool_name, tool_description, category_name, location_name,
                       is_available_now
                FROM tool_rental.ai_tool_search
                WHERE is_visible = TRUE AND LOWER(tool_name) = LOWER(:name)
                ORDER BY tool_id ASC
                """, Map.of("name", name), this::mapCandidate);
    }

    private ToolSearchCandidate mapCandidate(java.sql.ResultSet resultSet, int rowNumber)
            throws java.sql.SQLException {
        return new ToolSearchCandidate(
                resultSet.getInt("tool_id"),
                resultSet.getString("tool_name"),
                resultSet.getString("tool_description"),
                resultSet.getString("category_name"),
                resultSet.getString("location_name"),
                resultSet.getBoolean("is_available_now"));
    }

    public SearchResults searchWithCounts(ToolSearchSqlValidator.ValidatedSql query) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        String filters = filterSql(query.filters(), parameters);
        long total = count("is_visible = TRUE", filters, parameters);
        long available = count("is_available_now = TRUE", filters, parameters);
        return new SearchResults(search(query), total, available);
    }

    private long count(String visibilityCondition, String filters, MapSqlParameterSource parameters) {
        String sql = "SELECT COUNT(*) FROM tool_rental.ai_tool_search WHERE "
                + visibilityCondition + filters;
        Long result = jdbcTemplate.queryForObject(sql, parameters, Long.class);
        return result == null ? 0 : result;
    }

    private String filterSql(List<ToolSearchSqlValidator.FilterGroup> groups,
                             MapSqlParameterSource parameters) {
        StringBuilder sql = new StringBuilder();
        int parameterIndex = 0;
        for (ToolSearchSqlValidator.FilterGroup group : groups) {
            sql.append(" AND (");
            for (int alternativeIndex = 0; alternativeIndex < group.alternatives().size(); alternativeIndex++) {
                if (alternativeIndex > 0) sql.append(" OR ");
                ToolSearchSqlValidator.FilterCondition condition = group.alternatives().get(alternativeIndex);
                String parameter = "filter" + parameterIndex++;
                sql.append(condition.column()).append(' ').append(condition.operator())
                        .append(" :").append(parameter);
                parameters.addValue(parameter, condition.value());
            }
            sql.append(')');
        }
        return sql.toString();
    }

    public record SearchResults(List<AvailableTool> tools, long totalToolCount, long availableToolCount) {
        public SearchResults { tools = List.copyOf(tools); }
    }
}
