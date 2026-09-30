package ee.toolrental.persistence.city;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CityRepository extends JpaRepository<City, Integer> {
    @Query("select c from City c order by c.id")
    List<City> findAllCities();

}
