package ee.toolrental.persistence.category;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.sql.init.mode=never"
})
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void categoriesAreOrderedBySequenceThenId() {
        Category first = categoryRepository.saveAndFlush(category("Esimene", 300));
        Category second = categoryRepository.saveAndFlush(category("Teine", 100));
        Category third = categoryRepository.saveAndFlush(category("Kolmas", 100));

        assertTrue(first.getId() < second.getId());
        assertTrue(second.getId() < third.getId());

        List<Integer> ids = categoryRepository.findAllCategories().stream()
                .map(Category::getId)
                .toList();

        assertEquals(List.of(second.getId(), third.getId(), first.getId()), ids);
    }

    private Category category(String name, int sequence) {
        Category category = new Category();
        category.setCategoryName(name);
        category.setSequence(sequence);
        return category;
    }
}
