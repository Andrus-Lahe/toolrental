package ee.toolrental.service;

import ee.toolrental.persistence.category.Category;
import ee.toolrental.persistence.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor

public class CategoryService {

    private final CategoryRepository categoryRepository;


    public void getCategoriesInfo() {

        List<Category> allCategories = categoryRepository.findAllCategories();

    }


}
