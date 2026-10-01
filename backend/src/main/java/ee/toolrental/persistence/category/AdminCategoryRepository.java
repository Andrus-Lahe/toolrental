package ee.toolrental.persistence.category;

import ee.toolrental.controller.admin.dto.AdminCategoryDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

/**
 * Admini kategooriate nimekirja päring. Eraldi liides hoiab CategoryRepository muutmata,
 * et teised admini taskid (lisamine, muutmine, kustutamine) saaksid seda muuta ilma merge-konfliktita.
 */
public interface AdminCategoryRepository extends Repository<Category, Integer> {

    // Leiab kõik kategooriad admini tabeli jaoks järjestuses sequence ASC (võrdse sequence korral id ASC).
    // Päring projekteerib tulemuse otse AdminCategoryDto-sse; description võib olla null ja pilti (category_image) ei võeta.
    // Seda kasutab AdminCategoryListService.getCategories.
    @Query("""
            select new ee.toolrental.controller.admin.dto.AdminCategoryDto(
                c.id, c.categoryName, c.description, c.sequence)
            from Category c
            order by c.sequence asc, c.id asc""")
    List<AdminCategoryDto> findAllAdminCategoriesBy();
}
