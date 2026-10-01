package ee.toolrental.persistence.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest(properties = "spring.sql.init.mode=never")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AvailableToolRepository.class)
class AvailableToolRepositoryTest {
    @Autowired private JdbcTemplate jdbc;
    @Autowired private AvailableToolRepository repository;

    private String cityName;
    private String districtName;
    private String categoryName;
    private String toolName;

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString();
        cityName = "Testlinn " + suffix;
        districtName = "Testlinnaosa " + suffix;
        categoryName = "Testkategooria " + suffix;
        toolName = "Testtööriist " + suffix;

        int roleId = insertId("INSERT INTO tool_rental.role (role_name) VALUES (?) RETURNING id",
                "test-" + suffix.substring(0, 10));
        int cityId = insertId("INSERT INTO tool_rental.city (city_name) VALUES (?) RETURNING id", cityName);
        int districtId = insertId("INSERT INTO tool_rental.district (city_id, district_name) VALUES (?, ?) RETURNING id",
                cityId, districtName);
        int locationId = insertId("INSERT INTO tool_rental.location (district_id, street_name, house_number) " +
                "VALUES (?, 'Test tänav', '1') RETURNING id", districtId);
        int ownerId = insertId("INSERT INTO tool_rental.app_user " +
                "(role_id, first_name, last_name, google_sub, status) VALUES (?, 'Test', 'Omanik', ?, 'A') RETURNING id",
                roleId, "available-tools-test-" + suffix);
        jdbc.update("INSERT INTO tool_rental.profile (user_id, location_id, email, phone) VALUES (?, ?, ?, '55500000')",
                ownerId, locationId, "available-tools-" + suffix + "@example.com");
        int categoryId = insertId("INSERT INTO tool_rental.category (category_name, sequence) VALUES (?, 100) RETURNING id",
                categoryName);
        insertId("INSERT INTO tool_rental.tool (owner_id, category_id, name, status) VALUES (?, ?, ?, 'A') RETURNING id",
                ownerId, categoryId, toolName);
        insertId("INSERT INTO tool_rental.tool (owner_id, category_id, name, status) VALUES (?, ?, ?, 'U') RETURNING id",
                ownerId, categoryId, "Hõivatud " + suffix);
    }

    @Test
    void returnsOnlyAvailableToolsInRequestedDistrictWithoutProfileFields() {
        List<AvailableTool> tools = repository.findAvailable(cityName, districtName, categoryName);

        assertThat(tools).hasSize(1);
        assertThat(tools.getFirst()).isEqualTo(new AvailableTool(tools.getFirst().toolId(), toolName,
                categoryName, cityName, districtName));
        assertThat(tools.getFirst().toString()).doesNotContain("available-tools-");
    }

    @Test
    void readsFilterNamesFromPostgresToolRentalSchema() {
        assertThat(repository.cityNames()).contains(cityName);
        assertThat(repository.districtNames()).contains(districtName);
        assertThat(repository.categoryNames()).contains(categoryName);
    }

    private int insertId(String sql, Object... parameters) {
        return jdbc.queryForObject(sql, Integer.class, parameters);
    }
}
