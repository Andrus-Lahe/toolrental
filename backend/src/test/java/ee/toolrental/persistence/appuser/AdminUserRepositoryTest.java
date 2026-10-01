package ee.toolrental.persistence.appuser;

import ee.toolrental.controller.admin.dto.AdminUserDto;
import ee.toolrental.persistence.role.Role;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Kasutab kohalikku vali_it PostgreSQL andmebaasi koos 3_import.sql algandmetega.
 * Iga test jookseb transaktsioonis, mis lõpus tagasi võetakse, seega andmebaasi andmed ei muutu.
 */
@DataJpaTest(properties = "spring.sql.init.mode=never")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AdminUserRepositoryTest {

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private EntityManager entityManager;

    /**
     * Impordiandmete admin (id 1) ja Liis Kask (id 3) on loendis õigete väärtustega.
     * registeredAt on profiili loomise kuupäev ja järjestus on app_user.id kasvavalt.
     */
    @Test
    void importedUsersAreListedWithProfileValuesInIdOrder() {
        List<AdminUserDto> users = adminUserRepository.findAllUsersWithProfilesBy();

        AdminUserDto admin = findUser(users, 1);
        assertEquals("Marko", admin.getFirstName());
        assertEquals("Tamm", admin.getLastName());
        assertEquals("email@Gmail.com", admin.getEmail());
        assertEquals("admin", admin.getRoleName());
        assertEquals(LocalDate.of(2026, 9, 18), admin.getRegisteredAt());
        assertEquals("A", admin.getStatus());

        AdminUserDto customer = findUser(users, 3);
        assertEquals("Liis", customer.getFirstName());
        assertEquals("liis.kask@example.com", customer.getEmail());
        assertEquals("customer", customer.getRoleName());
        assertEquals(LocalDate.of(2026, 9, 18), customer.getRegisteredAt());

        List<Integer> ids = users.stream().map(AdminUserDto::getUserId).toList();
        assertEquals(ids.stream().sorted().toList(), ids);
    }

    /**
     * Profiilita ja blokeeritud kasutaja on loendis, tema email ja registeredAt on null.
     */
    @Test
    void blockedUserWithoutProfileIsListedWithNullEmailAndDate() {
        Role customerRole = entityManager.find(Role.class, 2);
        AppUser appUser = new AppUser();
        appUser.setRole(customerRole);
        appUser.setFirstName("Profiilita");
        appUser.setLastName("");
        appUser.setGoogleSub("test-sub-profiilita");
        appUser.setStatus("B");
        entityManager.persist(appUser);
        entityManager.flush();
        AppUser savedAppUser = appUser;

        AdminUserDto result = findUser(adminUserRepository.findAllUsersWithProfilesBy(), savedAppUser.getId());

        assertNull(result.getEmail());
        assertNull(result.getRegisteredAt());
        assertEquals("B", result.getStatus());
        assertEquals("customer", result.getRoleName());
        assertEquals("", result.getLastName());
        assertNotNull(result.getUserId());
    }

    /**
     * Otsib loendist kasutaja ID järgi ja nurjub selge sõnumiga, kui kasutajat pole.
     */
    private AdminUserDto findUser(List<AdminUserDto> users, Integer userId) {
        assertTrue(users.stream().anyMatch(user -> user.getUserId().equals(userId)), "Kasutaja puudub: " + userId);
        return users.stream().filter(user -> user.getUserId().equals(userId)).findFirst().orElseThrow();
    }
}
