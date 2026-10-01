package ee.toolrental.service;

import ee.toolrental.controller.admin.dto.CategoryUpdateRequestDto;
import ee.toolrental.infrastructure.exception.ForbiddenException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.persistence.category.AdminCategoryUpdateRepository;
import ee.toolrental.persistence.category.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AdminCategoryUpdateService {

    private static final String CATEGORY_UPDATING_FAILED = "Kategooria muutmine ebaõnnestus. Palun proovi hiljem uuesti.";
    private static final String CATEGORY_UNAVAILABLE_MESSAGE = "Sellise nimega kategooria on juba olemas";
    private static final String CATEGORY_UNAVAILABLE = "CATEGORY_UNAVAILABLE";

    private final AdminCategoryUpdateRepository adminCategoryUpdateRepository;
    private final CategoryService categoryService;

    /**
     * Asendab kategooria nime, kirjelduse ja järjekorranumbri; pilt ja tööriistad ei muutu.
     * Kontrollide järjekord: kategooria olemasolu (404), siis nime kasutus teisel kategoorial (403 CATEGORY_UNAVAILABLE).
     * Andmebaasi tõrge muudetakse 500 vastuseks; samaaegne duplikaat annab samuti 403.
     */
    @Transactional
    public void updateCategory(Integer categoryId, CategoryUpdateRequestDto categoryUpdateRequestDto) {
        try {
            Category category = categoryService.getValidCategoryBy(categoryId);
            validateNameIsAvailable(categoryUpdateRequestDto.getCategoryName(), categoryId);
            handleUpdateCategory(category, categoryUpdateRequestDto);
            adminCategoryUpdateRepository.saveAndFlush(category);
        } catch (DataIntegrityViolationException e) {
            throw new ForbiddenException(CATEGORY_UNAVAILABLE_MESSAGE, CATEGORY_UNAVAILABLE);
        } catch (DataAccessException e) {
            throw new InternalServerErrorException(CATEGORY_UPDATING_FAILED);
        }
    }

    /**
     * Kontrollib, et uus nimi ei kuulu mõnele teisele kategooriale; vastasel juhul visatakse 403.
     * Muudetava kategooria enda nimi on lubatud (päring välistab selle id).
     */
    private void validateNameIsAvailable(String categoryName, Integer categoryId) {
        if (adminCategoryUpdateRepository.existsOtherCategoryNamed(categoryName, categoryId)) {
            throw new ForbiddenException(CATEGORY_UNAVAILABLE_MESSAGE, CATEGORY_UNAVAILABLE);
        }
    }

    /**
     * Kirjutab päringu väärtused kategooria entiteedile üle (handle-prefiks: meetod muudab entiteeti).
     * Null kirjeldus kustutab olemasoleva kirjelduse, sest PUT asendab kogu sisu.
     */
    private void handleUpdateCategory(Category category, CategoryUpdateRequestDto categoryUpdateRequestDto) {
        category.setCategoryName(categoryUpdateRequestDto.getCategoryName());
        category.setDescription(categoryUpdateRequestDto.getDescription());
        category.setSequence(categoryUpdateRequestDto.getSequence());
    }
}
