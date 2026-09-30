package ee.toolrental.persistence.district;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DistrictRepository extends JpaRepository<District, Integer> {
    @Query("select d from District d where d.city.id = :cityId order by d.id")
    List<District> findDistrictsBy(Integer cityId);

}
