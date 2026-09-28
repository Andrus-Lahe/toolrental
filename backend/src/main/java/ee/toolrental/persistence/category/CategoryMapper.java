package ee.toolrental.persistence.category;


import ee.toolrental.controller.category.dto.CategoryDetailedInfoDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDetailedInfoDto toCategoryDetailedInfoDto(Category category);
}
