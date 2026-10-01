package ee.toolrental.persistence;

import ee.toolrental.persistence.appuser.AppUser;
import ee.toolrental.persistence.booking.Booking;
import ee.toolrental.persistence.booking.BookingRepository;
import ee.toolrental.persistence.category.Category;
import ee.toolrental.persistence.role.Role;
import ee.toolrental.persistence.tool.Tool;
import ee.toolrental.persistence.tool.ToolRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.sql.init.mode=never"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MyToolsQueryTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 25);

    @Autowired EntityManager entityManager;
    @Autowired BookingRepository bookingRepository;
    @Autowired ToolRepository toolRepository;

    private AppUser currentUser;
    private AppUser otherUser;
    private Tool incomingTool;
    private Tool rentedOutTool;
    private Tool freeTool;
    private Tool otherUsersTool;

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString();
        Role role = new Role();
        role.setRoleName("test-" + suffix.substring(0, 10));
        entityManager.persist(role);

        currentUser = user(role, "current-" + suffix);
        otherUser = user(role, "other-" + suffix);

        Category category = new Category();
        category.setCategoryName("MyTools test " + suffix);
        category.setSequence(1);
        entityManager.persist(category);

        incomingTool = tool(currentUser, category, "Ootel tööriist", "A", Instant.now());
        rentedOutTool = tool(currentUser, category, "Välja laenatud", "U", Instant.now().plusSeconds(1));
        Tool unavailableButNotRented = tool(currentUser, category, "U ilma broneeringuta", "U", Instant.now().plusSeconds(2));
        freeTool = tool(currentUser, category, "Vaba tööriist", "A", Instant.now().plusSeconds(3));
        otherUsersTool = tool(otherUser, category, "Teise kasutaja tööriist", "A", Instant.now());

        booking(incomingTool, otherUser, "P", TODAY, TODAY);
        booking(rentedOutTool, otherUser, "C", TODAY, TODAY);
        booking(otherUsersTool, currentUser, "C", TODAY, TODAY);
        booking(otherUsersTool, currentUser, "P", TODAY.plusDays(1), TODAY.plusDays(3));
        booking(otherUsersTool, currentUser, "P", TODAY.plusDays(2), TODAY.plusDays(4));
        booking(otherUsersTool, currentUser, "C", TODAY.plusDays(1), TODAY.plusDays(2));
        booking(otherUsersTool, currentUser, "P", TODAY.minusDays(4), TODAY.minusDays(1));

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void fiveQueriesApplyOwnershipStatusAndDateRulesWithoutCollapsingBookings() {
        var myRentals = bookingRepository.findCurrentRenterBookingsBy(currentUser.getId(), TODAY);
        var incoming = bookingRepository.findPendingOwnerBookingsBy(currentUser.getId(), TODAY);
        var outgoing = bookingRepository.findPendingRenterBookingsBy(currentUser.getId(), TODAY);
        var rentedOut = bookingRepository.findCurrentOwnerBookingsBy(currentUser.getId(), TODAY);
        var available = toolRepository.findAvailableOwnerToolsBy(currentUser.getId(), TODAY);

        assertEquals(2, myRentals.size(), "Käimasolev ja tulevane kinnitatud laenutus");
        assertEquals("C", myRentals.get(0).getStatus());
        assertEquals("C", myRentals.get(1).getStatus());
        assertEquals(TODAY.plusDays(1), myRentals.get(1).getStartDate());
        assertEquals(1, incoming.size());
        assertEquals("P", incoming.getFirst().getStatus());
        assertEquals(2, outgoing.size());
        assertNotEquals(outgoing.get(0).getId(), outgoing.get(1).getId());
        assertEquals(TODAY.plusDays(1), outgoing.get(0).getStartDate());
        assertEquals(1, rentedOut.size());
        assertEquals(rentedOutTool.getId(), rentedOut.getFirst().getTool().getId());
        assertTrue(available.stream().anyMatch(tool -> tool.getId().equals(incomingTool.getId())),
                "A pending request alone must not take an available tool out of the free list");
        assertTrue(available.stream().anyMatch(tool -> tool.getId().equals(freeTool.getId())));
        assertFalse(available.stream().anyMatch(tool -> tool.getId().equals(rentedOutTool.getId())));
        assertFalse(available.stream().anyMatch(tool -> tool.getStatus().equals("U")));
    }

    private AppUser user(Role role, String suffix) {
        AppUser user = new AppUser();
        user.setRole(role);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setGoogleSub("mytools-" + suffix);
        user.setStatus("A");
        entityManager.persist(user);
        return user;
    }

    private Tool tool(AppUser owner, Category category, String name, String status, Instant createdAt) {
        Tool tool = new Tool();
        tool.setOwner(owner);
        tool.setCategory(category);
        tool.setName(name);
        tool.setStatus(status);
        tool.setCreatedAt(createdAt);
        tool.setUpdatedAt(createdAt);
        entityManager.persist(tool);
        return tool;
    }

    private void booking(Tool tool, AppUser renter, String status, LocalDate startDate, LocalDate endDate) {
        Booking booking = new Booking();
        booking.setTool(tool);
        booking.setRenter(renter);
        booking.setStatus(status);
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);
        booking.setCreatedAt(Instant.now());
        booking.setUpdatedAt(Instant.now());
        entityManager.persist(booking);
    }
}
