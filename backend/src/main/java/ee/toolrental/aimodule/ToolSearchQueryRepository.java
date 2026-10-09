package ee.toolrental.aimodule;

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
}
