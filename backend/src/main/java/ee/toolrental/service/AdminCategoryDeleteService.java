package ee.toolrental.service;

import ee.toolrental.infrastructure.exception.ForbiddenException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.persistence.category.AdminCategoryDeleteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AdminCategoryDeleteService {

    private static final String CATEGORY_DELETING_FAILED = "Kategooria kustutamine ebaõnnestus. Palun proovi hiljem uuesti.";
    private static final String CATEGORY_IN_USE_MESSAGE = "Kategooriat ei saa kustutada, sest sellel on tööriistu";
    private static final String CATEGORY_IN_USE = "CATEGORY_IN_USE";

    private final AdminCategoryDeleteRepository adminCategoryDeleteRepository;
    private final CategoryService categoryService;

    /**
     * Kustutab kategooria ühes transaktsioonis: kõigepealt pildi (kui on), seejärel category rea.
     * Kontrollide järjekord: kategooria olemasolu (404), siis tööriistade olemasolu (403 CATEGORY_IN_USE).
     * Andmebaasi tõrge muudetakse 500 vastuseks ja kõik muudatused võetakse tagasi.
     */
    @Transactional
    public void deleteCategory(Integer categoryId) {
        try {
            categoryService.getValidCategoryBy(categoryId);
            validateHasNoTools(categoryId);
            adminCategoryDeleteRepository.deleteCategoryImageOf(categoryId);
            adminCategoryDeleteRepository.deleteCategoryBy(categoryId);
        } catch (DataAccessException e) {
            throw new InternalServerErrorException(CATEGORY_DELETING_FAILED);
        }
    }

    /**
     * Keelab kustutamise, kui kategoorias on vähemalt üks tööriist (403 CATEGORY_IN_USE).
     * Nii jäävad tool.category_id välisvõtmed terveks ja tööriistad ei kaota kategooriat.
     */
    private void validateHasNoTools(Integer categoryId) {
        if (adminCategoryDeleteRepository.existsToolInCategory(categoryId)) {
            throw new ForbiddenException(CATEGORY_IN_USE_MESSAGE, CATEGORY_IN_USE);
        }
    }
}
