package ee.toolrental.service;

import ee.toolrental.controller.category.dto.CategoryDetailedInfoDto;
import ee.toolrental.persistence.category.Category;
import ee.toolrental.persistence.category.CategoryMapper;
import ee.toolrental.persistence.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor

public class CategoryService {

    private final CategoryRepository categoryRepository;


    public List<CategoryDetailedInfoDto> getCategoriesInfo() {

        List<Category> allCategories = categoryRepository.findAllCategories();

        List<CategoryDetailedInfoDto>
                categoryDetailedInfoDtos = categoryMapper.toCategoryDetailedInfoDtos(allCategories);

        return categoryDetailedInfoDtos;

    }

    private final CategoryMapper categoryMapper;
}
