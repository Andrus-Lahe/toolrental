package ee.toolrental.persistence.booking;

import ee.toolrental.persistence.appuser.AppUser;
import ee.toolrental.persistence.category.Category;
import ee.toolrental.persistence.role.Role;
import ee.toolrental.persistence.tool.Tool;
import ee.toolrental.persistence.tool.ToolRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
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
class BookingRepositoryTest {
    @Autowired EntityManager entityManager;
    @Autowired BookingRepository bookingRepository;
    @Autowired ToolRepository toolRepository;

    private Tool tool;
    private AppUser renter;
    private final LocalDate start = LocalDate.now().plusYears(10);
    private final LocalDate end = start.plusDays(2);

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString();
        Role role = new Role();
        role.setRoleName("test-" + suffix.substring(0, 10));
        entityManager.persist(role);

        AppUser owner = user(role, "owner-" + suffix);
        renter = user(role, "renter-" + suffix);
        Category category = new Category();
        category.setCategoryName("Testkategooria " + suffix);
        category.setSequence(1);
        entityManager.persist(category);

        tool = new Tool();
        tool.setOwner(owner);
        tool.setCategory(category);
        tool.setName("Testtööriist " + suffix);
        tool.setStatus("A");
        tool.setCreatedAt(Instant.now());
        tool.setUpdatedAt(Instant.now());
        entityManager.persist(tool);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void overlapIncludesBothBoundariesForPendingAndConfirmed() {
        Integer bookingId = persistBooking("P");
        assertTrue(bookingRepository.existsActiveBookingOverlapping(tool.getId(), start.minusDays(1), start));
        assertTrue(bookingRepository.existsActiveBookingOverlapping(tool.getId(), end, end.plusDays(1)));
        assertFalse(bookingRepository.existsActiveBookingOverlapping(tool.getId(), end.plusDays(1), end.plusDays(2)));

        entityManager.createQuery("delete from Booking b where b.id = :id")
                .setParameter("id", bookingId).executeUpdate();
        persistBooking("C");
        assertTrue(bookingRepository.existsActiveBookingOverlapping(tool.getId(), start, end));
    }

    @Test
    void rejectedBookingDoesNotOccupyPeriodAndToolCanBeLocked() {
        persistBooking("R");
        assertFalse(bookingRepository.existsActiveBookingOverlapping(tool.getId(), start, end));
        assertEquals(tool.getId(), toolRepository.findToolForBookingBy(tool.getId()).orElseThrow().getId());
    }

    @Test
    void findsBookingAndFetchesToolOwnerAndRenter() {
        Integer bookingId = persistBooking("P");
        Booking booking = bookingRepository.findBookingWithPartiesById(bookingId).orElseThrow();
        assertEquals(tool.getId(), booking.getTool().getId());
        assertEquals("Test", booking.getTool().getOwner().getFirstName());
        assertEquals(renter.getId(), booking.getRenter().getId());
        assertTrue(bookingRepository.findBookingWithPartiesById(-1).isEmpty());
    }

    private AppUser user(Role role, String uniqueSuffix) {
        AppUser user = new AppUser();
        user.setRole(role);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setGoogleSub("booking-repository-" + uniqueSuffix);
        user.setStatus("A");
        entityManager.persist(user);
        return user;
    }

    private Integer persistBooking(String status) {
        Booking booking = new Booking();
        booking.setTool(entityManager.getReference(Tool.class, tool.getId()));
        booking.setRenter(entityManager.getReference(AppUser.class, renter.getId()));
        booking.setStartDate(start);
        booking.setEndDate(end);
        booking.setStatus(status);
        booking.setCreatedAt(Instant.now());
        booking.setUpdatedAt(Instant.now());
        entityManager.persist(booking);
        entityManager.flush();
        Integer bookingId = booking.getId();
        entityManager.clear();
        return bookingId;
    }
}
