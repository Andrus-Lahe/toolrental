package ee.toolrental.persistence.category;

import ee.toolrental.controller.admin.dto.AdminCategoryDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

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
class AdminCategoryCreateRepositoryTest {

    @Autowired
    private AdminCategoryCreateRepository adminCategoryCreateRepository;

    @Autowired
    private AdminCategoryRepository adminCategoryRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Olemasolev nimi annab true, uus nimi false; võrdlus on tõstutundlik (Aiatööd ja aiatööd on erinevad nimed).
     */
    @Test
    void nameCheckIsExactAndCaseSensitive() {
        assertTrue(adminCategoryCreateRepository.existsCategoryNamed("Aiatööd"));
        assertFalse(adminCategoryCreateRepository.existsCategoryNamed("aiatööd"));
        assertFalse(adminCategoryCreateRepository.existsCategoryNamed("Talvetööd-testkategooria"));
    }

    /**
     * Salvestatud kategooria (null kirjeldusega) on nähtav nii admini kui ka avalikus nimekirjas.
     */
    @Test
    void savedCategoryIsVisibleInAdminAndPublicLists() {
        Category category = new Category();
        category.setCategoryName("Talvetööd-testkategooria");
        category.setDescription(null);
        category.setSequence(400);

        Category savedCategory = adminCategoryCreateRepository.saveAndFlush(category);

        AdminCategoryDto adminCategoryDto = adminCategoryRepository.findAllAdminCategoriesBy().stream()
                .filter(adminCategory -> adminCategory.getCategoryId().equals(savedCategory.getId()))
                .findFirst().orElseThrow();
        assertEquals("Talvetööd-testkategooria", adminCategoryDto.getCategoryName());
        assertNull(adminCategoryDto.getDescription());
        assertEquals(400, adminCategoryDto.getSequence());

        List<Category> publicCategories = categoryRepository.findAllCategories();
        assertTrue(publicCategories.stream().anyMatch(publicCategory -> publicCategory.getId().equals(savedCategory.getId())));
    }

    /**
     * Sama nime teistkordne salvestamine lükatakse andmebaasi UNIQUE piirangu tõttu tagasi.
     * Nii tuvastab service samaaegse duplikaadi.
     */
    @Test
    void duplicateNameIsRejectedByDatabase() {
        Category category = new Category();
        category.setCategoryName("Aiatööd");
        category.setSequence(999);

        assertThrows(DataIntegrityViolationException.class, () -> adminCategoryCreateRepository.saveAndFlush(category));
    }
}
