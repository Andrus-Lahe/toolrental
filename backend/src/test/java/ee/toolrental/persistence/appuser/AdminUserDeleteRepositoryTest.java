package ee.toolrental.persistence.appuser;

import ee.toolrental.persistence.booking.Booking;
import ee.toolrental.persistence.category.Category;
import ee.toolrental.persistence.location.Location;
import ee.toolrental.persistence.profile.Profile;
import ee.toolrental.persistence.role.Role;
import ee.toolrental.persistence.tool.Tool;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Kasutab kohalikku vali_it PostgreSQL andmebaasi koos 3_import.sql algandmetega.
 * Iga test jookseb transaktsioonis, mis lõpus tagasi võetakse, seega andmebaasi andmed ei muutu.
 */
@DataJpaTest(properties = "spring.sql.init.mode=never")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AdminUserDeleteRepositoryTest {

    @Autowired
    private AdminUserDeleteRepository adminUserDeleteRepository;

    @Autowired
    private EntityManager entityManager;

    /**
     * Impordikasutaja 3 (Liis Kask) omab tööriistu ja üürib broneeringuid, seega mõlemad kontrollid annavad true.
     */
    @Test
    void importedUserHasToolsAndBookings() {
        assertTrue(adminUserDeleteRepository.existsToolOwnedBy(3));
        assertTrue(adminUserDeleteRepository.existsBookingRentedBy(3));
    }

    /**
     * Uus kasutaja ilma tööriistade ja broneeringuteta annab mõlemal kontrollil false.
     */
    @Test
    void newUserHasNoToolsOrBookings() {
        AppUser appUser = persistAppUser("test-sub-tuhi");

        assertFalse(adminUserDeleteRepository.existsToolOwnedBy(appUser.getId()));
        assertFalse(adminUserDeleteRepository.existsBookingRentedBy(appUser.getId()));
    }

    /**
     * Kasutaja, kellel on ainult tööriist, annab existsToolOwnedBy true ja existsBookingRentedBy false.
     */
    @Test
    void userWithOnlyTool_isDetectedAsToolOwner() {
        AppUser appUser = persistAppUser("test-sub-tool");
        persistTool(appUser);

        assertTrue(adminUserDeleteRepository.existsToolOwnedBy(appUser.getId()));
        assertFalse(adminUserDeleteRepository.existsBookingRentedBy(appUser.getId()));
    }

    /**
     * Kasutaja, kellel on ainult broneering (üürnikuna), annab existsBookingRentedBy true ja existsToolOwnedBy false.
     */
    @Test
    void userWithOnlyBooking_isDetectedAsRenter() {
        AppUser appUser = persistAppUser("test-sub-booking");
        Booking booking = new Booking();
        booking.setTool(entityManager.find(Tool.class, 1));
        booking.setRenter(appUser);
        booking.setStartDate(LocalDate.of(2030, 1, 1));
        booking.setEndDate(LocalDate.of(2030, 1, 2));
        booking.setStatus("P");
        booking.setCreatedAt(Instant.now());
        booking.setUpdatedAt(Instant.now());
        entityManager.persist(booking);
        entityManager.flush();

        assertTrue(adminUserDeleteRepository.existsBookingRentedBy(appUser.getId()));
        assertFalse(adminUserDeleteRepository.existsToolOwnedBy(appUser.getId()));
    }

    /**
     * Profiiliga kasutaja kustutamine: profiil ja kasutaja kustuvad (1 rida kummastki), location rida jääb alles.
     */
    @Test
    void deletesProfileAndUserButKeepsLocation() {
        AppUser appUser = persistAppUser("test-sub-profiil");
        Location location = entityManager.find(Location.class, 1);
        Profile profile = new Profile();
        profile.setUser(appUser);
        profile.setLocation(location);
        profile.setEmail("kustutatav.test@example.com");
        profile.setPhone("5550000");
        profile.setCreatedAt(Instant.now());
        profile.setUpdatedAt(Instant.now());
        entityManager.persist(profile);
        entityManager.flush();
        Integer userId = appUser.getId();

        assertEquals(1, adminUserDeleteRepository.deleteProfileOf(userId));
        assertEquals(1, adminUserDeleteRepository.deleteAppUserBy(userId));

        assertNull(entityManager.find(AppUser.class, userId));
        assertNotNull(entityManager.find(Location.class, 1));
    }

    /**
     * Profiilita kasutaja kustutamine: profiili kustutamine annab 0 rida, kasutaja kustub (1 rida).
     */
    @Test
    void userWithoutProfile_isDeleted() {
        AppUser appUser = persistAppUser("test-sub-ilma-profiilita");
        Integer userId = appUser.getId();

        assertEquals(0, adminUserDeleteRepository.deleteProfileOf(userId));
        assertEquals(1, adminUserDeleteRepository.deleteAppUserBy(userId));

        assertNull(entityManager.find(AppUser.class, userId));
    }

    /**
     * Loob ja salvestab antud kasutajale ühe tööriista olemasolevas kategoorias 1.
     */
    private void persistTool(AppUser owner) {
        Tool tool = new Tool();
        tool.setOwner(owner);
        tool.setCategory(entityManager.find(Category.class, 1));
        tool.setName("Testtööriist");
        tool.setStatus("A");
        tool.setCreatedAt(Instant.now());
        tool.setUpdatedAt(Instant.now());
        entityManager.persist(tool);
        entityManager.flush();
    }

    /**
     * Loob ja salvestab testkasutaja customer-rolliga (transaktsioon võetakse testi lõpus tagasi).
     */
    private AppUser persistAppUser(String googleSub) {
        AppUser appUser = new AppUser();
        appUser.setRole(entityManager.find(Role.class, 2));
        appUser.setFirstName("Test");
        appUser.setLastName("Kasutaja");
        appUser.setGoogleSub(googleSub);
        appUser.setStatus("A");
        entityManager.persist(appUser);
        entityManager.flush();
        return appUser;
    }
}
