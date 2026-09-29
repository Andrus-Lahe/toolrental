package ee.toolrental.persistence.categoryimage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryImageRepository
        extends JpaRepository<CategoryImage, Integer> {
    @Query("select e from CategoryImage e")
    List<CategoryImage> findAllCategoriesImages();
}
