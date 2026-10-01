package ee.toolrental.persistence.category;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

/**
 * Admini kategooria kustutamise päringud. Eraldi liides hoiab CategoryRepository, ToolRepository ja
 * CategoryImageRepository muutmata, et teised taskid saaksid neid muuta ilma merge-konfliktita.
 */
public interface AdminCategoryDeleteRepository extends Repository<Category, Integer> {

    // Kontrollib, kas kategoorias on vähemalt üks tööriist (tool.category_id); tööriista staatust (A/U) ei arvestata.
    // Tagastab true või false, mitte nimekirja. Seda kasutab AdminCategoryDeleteService.deleteCategory.
    @Query("select (count(t) > 0) from Tool t where t.category.id = :categoryId")
    boolean existsToolInCategory(Integer categoryId);

    // Kustutab kategooria pildi (category_image); kui pilti pole, ei kustutata midagi ja tagastatakse 0.
    // Pilt tuleb kustutada enne kategooriat, sest category_image.category_id välisvõtmel puudub ON DELETE CASCADE.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from CategoryImage ci where ci.category.id = :categoryId")
    int deleteCategoryImageOf(Integer categoryId);

    // Kustutab category rea ja tagastab kustutatud ridade arvu.
    // Kutsuda alles pärast deleteCategoryImageOf, samas transaktsioonis.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Category c where c.id = :categoryId")
    int deleteCategoryBy(Integer categoryId);
}
