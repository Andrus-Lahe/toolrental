package ee.toolrental.persistence.category;

import ee.toolrental.controller.category.dto.CategoryDetailedInfoDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(source = "id", target = "categoryId")
    @Mapping(source = "categoryName", target = "categoryName")
    @Mapping(source = "description", target = "categoryDescription")
    @Mapping(ignore = true, target = "imageData")
    CategoryDetailedInfoDto toCategoryDetailedInfoDto(Category category);
    List<CategoryDetailedInfoDto> toCategoryDetailedInfoDtos(List<Category> categories);
}
