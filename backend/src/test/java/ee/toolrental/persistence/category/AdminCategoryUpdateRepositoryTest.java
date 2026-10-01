package ee.toolrental.persistence.category;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Kasutab kohalikku vali_it PostgreSQL andmebaasi koos 3_import.sql algandmetega.
 * Iga test jookseb transaktsioonis, mis lõpus tagasi võetakse, seega andmebaasi andmed ei muutu.
 */
@DataJpaTest(properties = "spring.sql.init.mode=never")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AdminCategoryUpdateRepositoryTest {

    @Autowired
    private AdminCategoryUpdateRepository adminCategoryUpdateRepository;

    @Autowired
    private EntityManager entityManager;

    /**
     * Nime kontroll välistab muudetava kategooria enda: Aiatööd (id 1) enda nimi on lubatud,
     * Ehitustööd (id 2) nime kasutamine kategoorial 1 on aga keelatud; tõstutundlik võrdlus.
     */
    @Test
    void nameCheckExcludesOwnCategoryAndIsCaseSensitive() {
        assertFalse(adminCategoryUpdateRepository.existsOtherCategoryNamed("Aiatööd", 1));
        assertTrue(adminCategoryUpdateRepository.existsOtherCategoryNamed("Ehitustööd", 1));
        assertFalse(adminCategoryUpdateRepository.existsOtherCategoryNamed("ehitustööd", 1));
        assertFalse(adminCategoryUpdateRepository.existsOtherCategoryNamed("Üldse uus nimi", 1));
    }

    /**
     * Muutmine kirjutab üle kõik kolm välja (null kirjeldus jääb nulliks) ja jätab teised kategooriad puutumata.
     */
    @Test
    void updateOverwritesThreeFieldsAndLeavesOtherCategoriesUntouched() {
        Category category = adminCategoryUpdateRepository.findById(1).orElseThrow();
        category.setCategoryName("Aiatööd-testmuudetud");
        category.setDescription(null);
        category.setSequence(150);
        adminCategoryUpdateRepository.saveAndFlush(category);
        entityManager.clear();

        Category updatedCategory = adminCategoryUpdateRepository.findById(1).orElseThrow();
        assertEquals("Aiatööd-testmuudetud", updatedCategory.getCategoryName());
        assertNull(updatedCategory.getDescription());
        assertEquals(150, updatedCategory.getSequence());
        assertEquals("Ehitustööd", adminCategoryUpdateRepository.findById(2).orElseThrow().getCategoryName());
    }

    /**
     * Teise kategooria nime panemine lükatakse andmebaasi UNIQUE piirangu tõttu tagasi.
     * Nii tuvastab service samaaegse duplikaadi.
     */
    @Test
    void duplicateNameIsRejectedByDatabase() {
        Category category = adminCategoryUpdateRepository.findById(1).orElseThrow();
        category.setCategoryName("Ehitustööd");

        assertThrows(DataIntegrityViolationException.class, () -> adminCategoryUpdateRepository.saveAndFlush(category));
    }
}
