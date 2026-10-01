package ee.toolrental.persistence.category;

import ee.toolrental.controller.admin.dto.AdminCategoryDto;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Kasutab kohalikku vali_it PostgreSQL andmebaasi koos 3_import.sql algandmetega.
 * Iga test jookseb transaktsioonis, mis lõpus tagasi võetakse, seega andmebaasi andmed ei muutu.
 */
@DataJpaTest(properties = "spring.sql.init.mode=never")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AdminCategoryRepositoryTest {

    @Autowired
    private AdminCategoryRepository adminCategoryRepository;

    @Autowired
    private EntityManager entityManager;

    /**
     * Impordikategooriad 1-4 on loendis õigete väärtustega ja järjestus on sequence kasvavalt.
     */
    @Test
    void importedCategoriesAreListedWithValuesInSequenceOrder() {
        List<AdminCategoryDto> categories = adminCategoryRepository.findAllAdminCategoriesBy();

        AdminCategoryDto gardenCategory = findCategory(categories, 1);
        assertEquals("Aiatööd", gardenCategory.getCategoryName());
        assertEquals("Muruniidukid, labidad, rehad", gardenCategory.getDescription());
        assertEquals(100, gardenCategory.getSequence());

        AdminCategoryDto otherCategory = findCategory(categories, 4);
        assertEquals("Muud", otherCategory.getCategoryName());
        assertEquals(10000, otherCategory.getSequence());

        List<Integer> sequences = categories.stream().map(AdminCategoryDto::getSequence).toList();
        assertEquals(sequences.stream().sorted().toList(), sequences);
    }

    /**
     * Kategooria, mille kirjeldus on null, on loendis ja tema description on null.
     */
    @Test
    void categoryWithoutDescriptionIsListedWithNullDescription() {
        Category category = new Category();
        category.setCategoryName("Kirjeldusteta testkategooria");
        category.setDescription(null);
        category.setSequence(20000);
        entityManager.persist(category);
        entityManager.flush();

        AdminCategoryDto result = findCategory(adminCategoryRepository.findAllAdminCategoriesBy(), category.getId());

        assertNull(result.getDescription());
        assertEquals(20000, result.getSequence());
    }

    /**
     * Otsib loendist kategooria ID järgi ja nurjub selge sõnumiga, kui kategooriat pole.
     */
    private AdminCategoryDto findCategory(List<AdminCategoryDto> categories, Integer categoryId) {
        assertTrue(categories.stream().anyMatch(category -> category.getCategoryId().equals(categoryId)),
                "Kategooria puudub: " + categoryId);
        return categories.stream().filter(category -> category.getCategoryId().equals(categoryId)).findFirst().orElseThrow();
    }
}
