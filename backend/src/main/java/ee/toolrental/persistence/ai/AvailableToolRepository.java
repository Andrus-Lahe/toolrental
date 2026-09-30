package ee.toolrental.persistence.ai;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AvailableToolRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AvailableToolRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<String> cityNames() {
        return jdbcTemplate.getJdbcTemplate().queryForList(
                "SELECT city_name FROM tool_rental.city ORDER BY city_name", String.class);
    }

    public List<String> districtNames() {
        return jdbcTemplate.getJdbcTemplate().queryForList(
                "SELECT DISTINCT district_name FROM tool_rental.district ORDER BY district_name", String.class);
    }

    public List<String> categoryNames() {
        return jdbcTemplate.getJdbcTemplate().queryForList(
                "SELECT category_name FROM tool_rental.category ORDER BY category_name", String.class);
    }

    public List<AvailableTool> findAvailable(String city, String district, String category) {
        StringBuilder sql = new StringBuilder("""
                SELECT t.id AS tool_id, t.name, c.category_name, ci.city_name, d.district_name
                FROM tool_rental.tool t
                JOIN tool_rental.category c ON c.id = t.category_id
                LEFT JOIN tool_rental.profile p ON p.user_id = t.owner_id
                LEFT JOIN tool_rental.location l ON l.id = p.location_id
                LEFT JOIN tool_rental.district d ON d.id = l.district_id
                LEFT JOIN tool_rental.city ci ON ci.id = d.city_id
                WHERE t.status = 'A'
                """);

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        if (city != null) {
            sql.append(" AND ci.city_name = :city");
            parameters.addValue("city", city);
        }
        if (district != null) {
            sql.append(" AND d.district_name = :district");
            parameters.addValue("district", district);
        }
        if (category != null) {
            sql.append(" AND c.category_name = :category");
            parameters.addValue("category", category);
        }
        sql.append(" ORDER BY t.id LIMIT 20");

        return jdbcTemplate.query(sql.toString(), parameters, (rs, rowNum) -> new AvailableTool(
                rs.getInt("tool_id"),
                rs.getString("name"),
                rs.getString("category_name"),
                rs.getString("city_name"),
                rs.getString("district_name")));
    }
}
