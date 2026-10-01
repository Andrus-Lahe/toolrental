package ee.toolrental.persistence.appuser;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Kasutab kohalikku vali_it PostgreSQL andmebaasi koos 3_import.sql algandmetega.
 * Iga test jookseb transaktsioonis, mis lõpus tagasi võetakse, seega andmebaasi andmed ei muutu.
 */
@DataJpaTest(properties = "spring.sql.init.mode=never")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AdminUserStatusRepositoryTest {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private EntityManager entityManager;

    /**
     * Impordikasutaja 3 blokeerimine muudab andmebaasis ainult status veeru (B); nimi, roll ja google_sub jäävad samaks.
     * Seejärel taastab A olek algse seisu.
     */
    @Test
    void statusChangeAffectsOnlyStatusColumn() {
        AppUser appUser = appUserRepository.findById(3).orElseThrow();
        String firstName = appUser.getFirstName();
        String googleSub = appUser.getGoogleSub();
        Integer roleId = appUser.getRole().getId();

        appUser.setStatus("B");
        appUserRepository.saveAndFlush(appUser);
        entityManager.clear();

        AppUser blockedAppUser = appUserRepository.findById(3).orElseThrow();
        assertEquals("B", blockedAppUser.getStatus());
        assertEquals(firstName, blockedAppUser.getFirstName());
        assertEquals(googleSub, blockedAppUser.getGoogleSub());
        assertEquals(roleId, blockedAppUser.getRole().getId());

        blockedAppUser.setStatus("A");
        appUserRepository.saveAndFlush(blockedAppUser);
        entityManager.clear();

        assertEquals("A", appUserRepository.findById(3).orElseThrow().getStatus());
    }
}
