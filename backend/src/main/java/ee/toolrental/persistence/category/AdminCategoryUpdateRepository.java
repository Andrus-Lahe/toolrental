package ee.toolrental.persistence.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Admini kategooria muutmise päringud. Eraldi liides hoiab CategoryRepository muutmata,
 * et teised admini taskid (lisamine, kustutamine) saaksid seda muuta ilma merge-konfliktita.
 */
public interface AdminCategoryUpdateRepository extends JpaRepository<Category, Integer> {

    // Kontrollib, kas sama nimi kuulub mõnele TEISELE kategooriale (id <> categoryId); võrdlus on täpne ja tõstutundlik.
    // Muudetava kategooria enda nime uuesti salvestamine on seega lubatud. Seda kasutab AdminCategoryUpdateService.updateCategory.
    @Query("select (count(c) > 0) from Category c where c.categoryName = :categoryName and c.id <> :categoryId")
    boolean existsOtherCategoryNamed(String categoryName, Integer categoryId);
}
