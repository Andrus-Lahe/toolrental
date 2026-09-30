package ee.toolrental.persistence.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AvailableToolRepositoryTest {

    private AvailableToolRepository repository;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:available_tools;DB_CLOSE_DELAY=-1", "sa", "");
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("DROP ALL OBJECTS");
        jdbc.execute("CREATE SCHEMA tool_rental");
        jdbc.execute("CREATE TABLE tool_rental.category (id INT PRIMARY KEY, category_name VARCHAR(100))");
        jdbc.execute("CREATE TABLE tool_rental.city (id INT PRIMARY KEY, city_name VARCHAR(100))");
        jdbc.execute("CREATE TABLE tool_rental.district (id INT PRIMARY KEY, city_id INT, district_name VARCHAR(100))");
        jdbc.execute("CREATE TABLE tool_rental.location (id INT PRIMARY KEY, district_id INT)");
        jdbc.execute("CREATE TABLE tool_rental.profile (id INT PRIMARY KEY, user_id INT, location_id INT, email VARCHAR(100))");
        jdbc.execute("CREATE TABLE tool_rental.tool (id INT PRIMARY KEY, owner_id INT, category_id INT, name VARCHAR(100), status CHAR(1))");

        jdbc.execute("INSERT INTO tool_rental.category VALUES (1, 'Puurid')");
        jdbc.execute("INSERT INTO tool_rental.city VALUES (1, 'Tallinn')");
        jdbc.execute("INSERT INTO tool_rental.district VALUES (1, 1, 'Kristiine'), (2, 1, 'Lasnamäe')");
        jdbc.execute("INSERT INTO tool_rental.location VALUES (1, 1), (2, 2)");
        jdbc.execute("INSERT INTO tool_rental.profile VALUES (1, 10, 1, 'private@example.com'), (2, 20, 2, 'other@example.com')");
        jdbc.execute("INSERT INTO tool_rental.tool VALUES (1, 10, 1, 'Akutrell', 'A'), (2, 10, 1, 'Redel', 'U'), (3, 20, 1, 'Puurvasar', 'A')");

        repository = new AvailableToolRepository(new NamedParameterJdbcTemplate(dataSource));
    }

    @Test
    void returnsOnlyAvailableToolsInRequestedDistrictWithoutProfileFields() {
        List<AvailableTool> tools = repository.findAvailable(null, "Kristiine", null);

        assertThat(tools).containsExactly(
                new AvailableTool(1, "Akutrell", "Puurid", "Tallinn", "Kristiine"));
        assertThat(tools.getFirst().toString()).doesNotContain("private@example.com");
    }

    @Test
    void readsFilterNamesFromToolRentalSchema() {
        assertThat(repository.cityNames()).containsExactly("Tallinn");
        assertThat(repository.districtNames()).containsExactly("Kristiine", "Lasnamäe");
        assertThat(repository.categoryNames()).containsExactly("Puurid");
    }
}
