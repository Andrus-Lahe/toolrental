package ee.toolrental.service;

import ee.toolrental.controller.admin.dto.CategoryCreateRequestDto;
import ee.toolrental.infrastructure.exception.ForbiddenException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.persistence.category.AdminCategoryCreateRepository;
import ee.toolrental.persistence.category.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AdminCategoryCreateService {

    private static final String CATEGORY_CREATING_FAILED = "Kategooria lisamine ebaõnnestus. Palun proovi hiljem uuesti.";
    private static final String CATEGORY_UNAVAILABLE_MESSAGE = "Sellise nimega kategooria on juba olemas";
    private static final String CATEGORY_UNAVAILABLE = "CATEGORY_UNAVAILABLE";

    private final AdminCategoryCreateRepository adminCategoryCreateRepository;

    /**
     * Lisab uue kategooria (nimi, kirjeldus, järjekorranumber); pilti selles versioonis ei lisata.
     * Sama nimi annab 403 CATEGORY_UNAVAILABLE, ka siis, kui duplikaat tekib samaaegselt (andmebaasi UNIQUE piirang).
     * Muu andmebaasi tõrge muudetakse 500 vastuseks ja muudatus võetakse tagasi.
     */
    @Transactional
    public void createCategory(CategoryCreateRequestDto categoryCreateRequestDto) {
        try {
            validateNameIsAvailable(categoryCreateRequestDto.getCategoryName());
            adminCategoryCreateRepository.saveAndFlush(toCategory(categoryCreateRequestDto));
        } catch (DataIntegrityViolationException e) {
            throw new ForbiddenException(CATEGORY_UNAVAILABLE_MESSAGE, CATEGORY_UNAVAILABLE);
        } catch (DataAccessException e) {
            throw new InternalServerErrorException(CATEGORY_CREATING_FAILED);
        }
    }

    /**
     * Kontrollib enne salvestamist, et sama nimega kategooriat ei ole (täpne võrdlus); vastasel juhul visatakse 403.
     * Nii saab kasutaja arusaadava veateate, mitte andmebaasi erindi.
     */
    private void validateNameIsAvailable(String categoryName) {
        if (adminCategoryCreateRepository.existsCategoryNamed(categoryName)) {
            throw new ForbiddenException(CATEGORY_UNAVAILABLE_MESSAGE, CATEGORY_UNAVAILABLE);
        }
    }

    /**
     * Teisendab päringu DTO uueks Category entiteediks (id genereeritakse andmebaasis).
     * Kirjeldus jääb null, kui seda päringus ei olnud.
     */
    private Category toCategory(CategoryCreateRequestDto categoryCreateRequestDto) {
        Category category = new Category();
        category.setCategoryName(categoryCreateRequestDto.getCategoryName());
        category.setDescription(categoryCreateRequestDto.getDescription());
        category.setSequence(categoryCreateRequestDto.getSequence());
        return category;
    }
}
