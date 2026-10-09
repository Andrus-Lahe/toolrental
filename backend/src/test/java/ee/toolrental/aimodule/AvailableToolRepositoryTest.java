package ee.toolrental.aimodule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest(properties = "spring.sql.init.mode=never")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({AvailableToolRepository.class, AvailableToolRepositoryTest.FixedClockConfiguration.class})
class AvailableToolRepositoryTest {
    @Autowired private JdbcTemplate jdbc;
    @Autowired private AvailableToolRepository repository;
    @Autowired private Clock clock;

    private String cityName;
    private String districtName;
    private String categoryName;
    private String toolName;
    private int ownerId;
    private int availableToolId;

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
        ownerId = insertId("INSERT INTO tool_rental.app_user " +
                "(role_id, first_name, last_name, google_sub, status) VALUES (?, 'Test', 'Omanik', ?, 'A') RETURNING id",
                roleId, "available-tools-test-" + suffix);
        jdbc.update("INSERT INTO tool_rental.profile (user_id, location_id, email, phone) VALUES (?, ?, ?, '55500000')",
                ownerId, locationId, "available-tools-" + suffix + "@example.com");
        int categoryId = insertId("INSERT INTO tool_rental.category (category_name, sequence) VALUES (?, 100) RETURNING id",
                categoryName);
        availableToolId = insertId("INSERT INTO tool_rental.tool (owner_id, category_id, name, status) VALUES (?, ?, ?, 'A') RETURNING id",
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

    @Test
    void confirmedBookingCoveringTodayBlocksToolInclusively() {
        LocalDate today = LocalDate.now(clock);
        insertBooking(availableToolId, "C", today, today);

        assertThat(repository.findAvailable(cityName, districtName, categoryName)).isEmpty();
    }

    @Test
    void pendingAndRejectedBookingsDoNotBlockTool() {
        LocalDate today = LocalDate.now(clock);
        insertBooking(availableToolId, "P", today, today);
        insertBooking(availableToolId, "R", today, today);

        assertThat(repository.findAvailable(cityName, districtName, categoryName))
                .extracting(AvailableTool::name)
                .containsExactly(toolName);
    }

    @Test
    void confirmedBookingsOutsideTodayDoNotBlockTool() {
        LocalDate today = LocalDate.now(clock);
        insertBooking(availableToolId, "C", today.minusDays(1), today.minusDays(1));
        insertBooking(availableToolId, "C", today.plusDays(1), today.plusDays(1));

        assertThat(repository.findAvailable(cityName, districtName, categoryName))
                .extracting(AvailableTool::name)
                .containsExactly(toolName);
    }

    @Test
    void clockUsesTallinnDateAtUtcDayBoundary() {
        assertThat(clock.getZone()).isEqualTo(ZoneId.of("Europe/Tallinn"));
        assertThat(LocalDate.now(clock)).isEqualTo(LocalDate.of(2025, 1, 2));
    }

    private void insertBooking(int toolId, String status, LocalDate startDate, LocalDate endDate) {
        jdbc.update("INSERT INTO tool_rental.booking " +
                        "(tool_id, renter_id, start_date, end_date, status) VALUES (?, ?, ?, ?, ?)",
                toolId, ownerId, startDate, endDate, status);
    }

    @TestConfiguration
    static class FixedClockConfiguration {
        @Bean
        @Primary
        Clock testClock() {
            return Clock.fixed(Instant.parse("2025-01-01T22:30:00Z"), ZoneId.of("Europe/Tallinn"));
        }
    }

    private int insertId(String sql, Object... parameters) {
        return jdbc.queryForObject(sql, Integer.class, parameters);
    }
}
