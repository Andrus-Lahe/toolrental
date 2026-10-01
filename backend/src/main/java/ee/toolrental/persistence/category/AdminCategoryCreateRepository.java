package ee.toolrental.persistence.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Admini kategooria lisamise päringud. Eraldi liides hoiab CategoryRepository muutmata,
 * et teised admini taskid (muutmine, kustutamine) saaksid seda muuta ilma merge-konfliktita.
 */
public interface AdminCategoryCreateRepository extends JpaRepository<Category, Integer> {

    // Kontrollib, kas sama nimega kategooria on juba olemas; võrdlus on täpne ja tõstutundlik, nagu andmebaasi UNIQUE piirang.
    // Tagastab true või false, mitte kategooria. Seda kasutab AdminCategoryCreateService.createCategory enne salvestamist.
    @Query("select (count(c) > 0) from Category c where c.categoryName = :categoryName")
    boolean existsCategoryNamed(String categoryName);
}
