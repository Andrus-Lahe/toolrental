package ee.toolrental.service;

import ee.toolrental.controller.category.dto.CategoryDetailedInfoDto;
import ee.toolrental.controller.category.dto.CategoryDto;
import ee.toolrental.infrastructure.exception.CategoryLoadingException;
import ee.toolrental.persistence.category.Category;
import ee.toolrental.persistence.category.CategoryMapper;
import ee.toolrental.persistence.category.CategoryRepository;
import ee.toolrental.persistence.categoryimage.CategoryImage;
import ee.toolrental.persistence.categoryimage.CategoryImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final CategoryImageRepository categoryImageRepository;

    public List<CategoryDto> getCategories() {
        try {
            List<Category> allCategories = categoryRepository.findAllCategories();
            List<CategoryDto> categoryDtos = categoryMapper.toCategoryDtos(allCategories);
            return categoryDtos;
        } catch (Exception exception) {
            throw new CategoryLoadingException("Kategooriate laadimine ebaõnnestus. Palun proovi hiljem uuesti.",
                    "INTERNAL_SERVER_ERROR");
        }
    }

    public List<CategoryDetailedInfoDto> getCategoriesInfo() {
        try {
            List<Category> allCategories = categoryRepository.findAllCategories();
            List<CategoryDetailedInfoDto>
                    categoryDetailedInfoDtos = categoryMapper.toCategoryDetailedInfoDtos(allCategories);
            List<CategoryImage> categoryImages = categoryImageRepository.findAllCategoriesImages();

            for (CategoryDetailedInfoDto categoryDetailedInfoDto : categoryDetailedInfoDtos) {
                for (CategoryImage categoryImage : categoryImages) {
                    if
                    (categoryDetailedInfoDto.getCategoryId().equals(categoryImage.getCategory().getId())) {
                        byte[] imageBytes = categoryImage.getImageData();
                        String bytesToString = Base64.getEncoder().encodeToString(imageBytes);
                        categoryDetailedInfoDto.setImageData(bytesToString);
                    }
                }
            }

            return categoryDetailedInfoDtos;
        } catch (Exception exception) {
            throw new CategoryLoadingException("Kategooriate laadimine ebaõnnestus. Palun proovi hiljem uuesti.",
                    "INTERNAL_SERVER_ERROR");
        }
    }
}