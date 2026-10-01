package ee.toolrental.service;

import ee.toolrental.controller.admin.dto.AdminCategoryDto;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.persistence.category.AdminCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminCategoryListService {

    private static final String CATEGORIES_LOADING_FAILED = "Kategooriate laadimine ebaõnnestus. Palun proovi hiljem uuesti.";

    private final AdminCategoryRepository adminCategoryRepository;

    /**
     * Tagastab kõik kategooriad admini tabeli jaoks järjestuses sequence ASC, koos kirjelduse ja järjekorranumbriga.
     * Kategooriateta andmebaasi korral on tulemus tühi loend; andmebaasi tõrge muudetakse 500 vastuseks.
     */
    @Transactional(readOnly = true)
    public List<AdminCategoryDto> getCategories() {
        try {
            return adminCategoryRepository.findAllAdminCategoriesBy();
        } catch (DataAccessException e) {
            throw new InternalServerErrorException(CATEGORIES_LOADING_FAILED);
        }
    }
}
