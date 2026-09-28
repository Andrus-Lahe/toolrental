package ee.toolrental.service;

import ee.toolrental.controller.category.dto.CategoryDetailedInfoDto;
import ee.toolrental.persistence.category.Category;
import ee.toolrental.persistence.category.CategoryMapper;
import ee.toolrental.persistence.category.CategoryRepository;
import ee.toolrental.persistence.categoryimage.CategoryImage;
import ee.toolrental.persistence.categoryimage.CategoryImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor

public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryImageRepository categoryImageRepository;


    public List<CategoryDetailedInfoDto> getCategoriesInfo() {

        List<Category> allCategories = categoryRepository.findAllCategories();

        List<CategoryDetailedInfoDto>
                categoryDetailedInfoDtos = categoryMapper.toCategoryDetailedInfoDtos(allCategories);


        List<CategoryImage> categoryImages = categoryImageRepository.findAllCategoriesImages();

        return categoryDetailedInfoDtos;

    }

    private final CategoryMapper categoryMapper;


}
