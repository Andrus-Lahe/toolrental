package ee.toolrental.persistence.category;

import ee.toolrental.persistence.appuser.AppUser;
import ee.toolrental.persistence.categoryimage.CategoryImage;
import ee.toolrental.persistence.tool.Tool;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.Instant;

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
class AdminCategoryDeleteRepositoryTest {

    @Autowired
    private AdminCategoryDeleteRepository adminCategoryDeleteRepository;

    @Autowired
    private EntityManager entityManager;

    /**
     * Impordikategooria 4 (Muud) sisaldab tööriistu, uus tühi kategooria mitte.
     */
    @Test
    void importedCategoryHasToolsAndNewCategoryDoesNot() {
        Category emptyCategory = persistCategory("Tuhi-testkategooria");

        assertTrue(adminCategoryDeleteRepository.existsToolInCategory(4));
        assertFalse(adminCategoryDeleteRepository.existsToolInCategory(emptyCategory.getId()));
    }

    /**
     * Kategooria, milles on ainult kasutamata (staatus U) tööriist, takistab samuti kustutamist,
     * sest tööriista staatust ei arvestata.
     */
    @Test
    void unavailableToolStillBlocksDeleting() {
        Category category = persistCategory("Tooriistaga-testkategooria");
        AppUser owner = entityManager.find(AppUser.class, 1);
        Tool tool = new Tool();
        tool.setOwner(owner);
        tool.setCategory(category);
        tool.setName("Testtööriist");
        tool.setStatus("U");
        tool.setCreatedAt(Instant.now());
        tool.setUpdatedAt(Instant.now());
        entityManager.persist(tool);
        entityManager.flush();

        assertTrue(adminCategoryDeleteRepository.existsToolInCategory(category.getId()));
    }

    /**
     * Pildiga kategooria kustutamine: pilt ja kategooria kustuvad (1 rida kummastki).
     */
    @Test
    void deletesImageAndCategory() {
        Category category = persistCategory("Pildiga-testkategooria");
        CategoryImage categoryImage = new CategoryImage();
        categoryImage.setCategory(category);
        categoryImage.setImageData(new byte[]{1, 2, 3});
        entityManager.persist(categoryImage);
        entityManager.flush();
        Integer categoryId = category.getId();

        assertEquals(1, adminCategoryDeleteRepository.deleteCategoryImageOf(categoryId));
        assertEquals(1, adminCategoryDeleteRepository.deleteCategoryBy(categoryId));

        assertNull(entityManager.find(Category.class, categoryId));
    }

    /**
     * Pildita kategooria kustutamine: pildi kustutamine annab 0 rida, kategooria kustub (1 rida); teised kategooriad jäävad.
     */
    @Test
    void categoryWithoutImage_isDeletedAndOthersRemain() {
        Category category = persistCategory("Pildita-testkategooria");
        Integer categoryId = category.getId();

        assertEquals(0, adminCategoryDeleteRepository.deleteCategoryImageOf(categoryId));
        assertEquals(1, adminCategoryDeleteRepository.deleteCategoryBy(categoryId));

        assertNull(entityManager.find(Category.class, categoryId));
        assertNotNull(entityManager.find(Category.class, 1));
    }

    /**
     * Loob ja salvestab testkategooria (transaktsioon võetakse testi lõpus tagasi).
     */
    private Category persistCategory(String categoryName) {
        Category category = new Category();
        category.setCategoryName(categoryName);
        category.setDescription(null);
        category.setSequence(30000);
        entityManager.persist(category);
        entityManager.flush();
        return category;
    }
}
