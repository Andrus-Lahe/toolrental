package ee.toolrental.aimodule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest(properties = "spring.sql.init.mode=never")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ToolSearchQueryRepository.class, ToolSearchSqlValidator.class})
class ToolSearchQueryRepositoryTest {

    @Autowired private JdbcTemplate jdbc;
    @Autowired private ToolSearchQueryRepository repository;
    @Autowired private ToolSearchSqlValidator validator;

    private String toolName;
    private String categoryName;
    private String districtName;
    private int ownerId;
    private int toolId;

    @BeforeEach
    void insertSearchFixture() {
        String suffix = UUID.randomUUID().toString();
        toolName = "AI otsingu test " + suffix;
        categoryName = "AI kategooria " + suffix;
        districtName = "AI linnaosa " + suffix;

        int roleId = insertId("INSERT INTO tool_rental.role (role_name) VALUES (?) RETURNING id",
                "ai-" + suffix.substring(0, 10));
        int cityId = insertId("INSERT INTO tool_rental.city (city_name) VALUES (?) RETURNING id",
                "AI linn " + suffix);
        int districtId = insertId("INSERT INTO tool_rental.district " +
                "(city_id, district_name) VALUES (?, ?) RETURNING id", cityId, districtName);
        int locationId = insertId("INSERT INTO tool_rental.location " +
                "(district_id, street_name, house_number) VALUES (?, 'Test tänav', '1') RETURNING id", districtId);
        ownerId = insertId("INSERT INTO tool_rental.app_user " +
                "(role_id, first_name, last_name, google_sub, status) " +
                "VALUES (?, 'AI', 'Otsing', ?, 'A') RETURNING id", roleId, "ai-search-test-" + suffix);
        jdbc.update("INSERT INTO tool_rental.profile (user_id, location_id, email, phone) " +
                "VALUES (?, ?, ?, '55500000')", ownerId, locationId, "ai-search-" + suffix + "@example.com");
        int categoryId = insertId("INSERT INTO tool_rental.category " +
                "(category_name, sequence) VALUES (?, 100) RETURNING id", categoryName);
        toolId = insertId("INSERT INTO tool_rental.tool (owner_id, category_id, name, status) " +
                "VALUES (?, ?, ?, 'A') RETURNING id", ownerId, categoryId, toolName);
        jdbc.update("UPDATE tool_rental.tool SET description = 'Sobib kaevamiseks.' WHERE id = ?", toolId);
    }

    @Test
    void executesValidatedQueryAgainstViewAndMapsOnlySearchFields() {
        String sql = "SELECT tool_id, tool_name, category_name, location_name " +
                "FROM tool_rental.ai_tool_search WHERE is_available_now = TRUE " +
                "AND tool_name = '" + toolName + "' ORDER BY tool_id LIMIT 20";

        List<AvailableTool> results = repository.search(validator.validate(sql));

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().name()).isEqualTo(toolName);
        assertThat(results.getFirst().category()).isEqualTo(categoryName);
        assertThat(results.getFirst().location()).isEqualTo(districtName);
    }

    @Test
    void confirmedBookingCoveringTodayIsFilteredByView() {
        insertBooking("C");

        assertThat(searchByToolName()).isEmpty();
    }

    @Test
    void countsConfirmedBookingInTotalButNotAvailableCountOrToolList() {
        insertBooking("C");

        ToolSearchQueryRepository.SearchResults results = repository.searchWithCounts(
                validator.validate(searchSql("tool_name = '" + toolName + "'")));

        assertThat(results.totalToolCount()).isEqualTo(1);
        assertThat(results.availableToolCount()).isZero();
        assertThat(results.tools()).isEmpty();
    }

    @Test
    void pendingAndRejectedBookingsDoNotHideToolFromView() {
        insertBooking("P");
        insertBooking("R");

        ToolSearchQueryRepository.SearchResults results = repository.searchWithCounts(
                validator.validate(searchSql("tool_name = '" + toolName + "'")));

        assertThat(results.totalToolCount()).isEqualTo(1);
        assertThat(results.availableToolCount()).isEqualTo(1);
        assertThat(results.tools()).hasSize(1);
    }

    @Test
    void excludesUnpublishedToolFromBothCounts() {
        jdbc.update("UPDATE tool_rental.tool SET status = 'U' WHERE id = ?", toolId);

        ToolSearchQueryRepository.SearchResults results = repository.searchWithCounts(
                validator.validate(searchSql("tool_name = '" + toolName + "'")));

        assertThat(results.totalToolCount()).isZero();
        assertThat(results.availableToolCount()).isZero();
        assertThat(results.tools()).isEmpty();
    }

    @Test
    void noMatchesReturnZeroCountsAndEmptyToolList() {
        ToolSearchQueryRepository.SearchResults results = repository.searchWithCounts(
                validator.validate(searchSql("tool_id = 0")));

        assertThat(results.totalToolCount()).isZero();
        assertThat(results.availableToolCount()).isZero();
        assertThat(results.tools()).isEmpty();
    }

    @Test
    void resultLimitDoesNotTruncateEitherCount() {
        for (int index = 0; index < 20; index++) {
            insertId("INSERT INTO tool_rental.tool (owner_id, category_id, name, status) " +
                    "SELECT ?, id, ?, 'A' FROM tool_rental.category WHERE category_name = ? RETURNING id",
                    ownerId, toolName + " extra " + index, categoryName);
        }

        ToolSearchQueryRepository.SearchResults results = repository.searchWithCounts(
                validator.validate(searchSql("category_name = '" + categoryName + "'")));

        assertThat(results.totalToolCount()).isEqualTo(21);
        assertThat(results.availableToolCount()).isEqualTo(21);
        assertThat(results.tools()).hasSize(20);
    }

    @Test
    void appliesSameDescriptionOrLocationFiltersToCountsAndToolResults() {
        String sql = "SELECT tool_id, tool_name, category_name, location_name " +
                "FROM tool_rental.ai_tool_search WHERE is_available_now = TRUE " +
                "AND (tool_name ILIKE '%labidas%' OR tool_description ILIKE '%kaevamiseks%') " +
                "AND location_name = '" + districtName + "' ORDER BY tool_id LIMIT 20";

        ToolSearchQueryRepository.SearchResults results = repository.searchWithCounts(validator.validate(sql));

        assertThat(results.totalToolCount()).isEqualTo(1);
        assertThat(results.availableToolCount()).isEqualTo(1);
        assertThat(results.tools()).extracting(AvailableTool::toolId).containsExactly(toolId);
    }

    private List<AvailableTool> searchByToolName() {
        return repository.search(validator.validate(searchSql("tool_name = '" + toolName + "'")));
    }

    private String searchSql(String filter) {
        return "SELECT tool_id, tool_name, category_name, location_name " +
                "FROM tool_rental.ai_tool_search WHERE is_available_now = TRUE AND " + filter +
                " ORDER BY tool_id LIMIT 20";
    }

    private void insertBooking(String status) {
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Tallinn"));
        jdbc.update("INSERT INTO tool_rental.booking " +
                        "(tool_id, renter_id, start_date, end_date, status) VALUES (?, ?, ?, ?, ?)",
                toolId, ownerId, today, today, status);
    }

    private int insertId(String sql, Object... parameters) {
        return jdbc.queryForObject(sql, Integer.class, parameters);
    }
}
