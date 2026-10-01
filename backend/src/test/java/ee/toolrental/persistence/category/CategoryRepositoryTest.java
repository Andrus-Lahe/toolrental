package ee.toolrental.persistence.category;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.UUID;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=none", "spring.sql.init.mode=never"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void categoriesAreOrderedBySequenceThenId() {
        String suffix = UUID.randomUUID().toString();
        Category first = categoryRepository.saveAndFlush(category("Esimene-" + suffix, 300));
        Category second = categoryRepository.saveAndFlush(category("Teine-" + suffix, 100));
        Category third = categoryRepository.saveAndFlush(category("Kolmas-" + suffix, 100));

        assertTrue(first.getId() < second.getId());
        assertTrue(second.getId() < third.getId());

        List<Integer> insertedCategoryIds = List.of(first.getId(), second.getId(), third.getId());
        List<Integer> ids = categoryRepository.findAllCategories().stream()
                .filter(category -> insertedCategoryIds.contains(category.getId()))
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
