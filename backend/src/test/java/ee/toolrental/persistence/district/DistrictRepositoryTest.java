package ee.toolrental.persistence.district;

import ee.toolrental.persistence.city.City;
import ee.toolrental.persistence.city.CityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Kasutab kohalikku vali_it PostgreSQL andmebaasi koos 3_import.sql algandmetega.
 * Iga test jookseb transaktsioonis, mis lõpus tagasi võetakse, seega andmebaasi andmed ei muutu.
 */
@DataJpaTest(properties = "spring.sql.init.mode=never")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DistrictRepositoryTest {

    @Autowired
    private DistrictRepository districtRepository;

    @Autowired
    private CityRepository cityRepository;

    @Test
    void tallinnHasEightDistrictsOrderedById() {
        List<District> districts = districtRepository.findDistrictsBy(1);

        List<Integer> ids = districts.stream()
                .map(District::getId)
                .toList();

        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8), ids);
        assertEquals("Kristiine", districts.getFirst().getDistrictName());
    }

    @Test
    void tartuHasEighteenAndParnuSevenDistricts() {
        List<District> tartuDistricts = districtRepository.findDistrictsBy(2);
        List<District> parnuDistricts = districtRepository.findDistrictsBy(3);

        assertEquals(18, tartuDistricts.size());
        assertEquals(7, parnuDistricts.size());
        assertTrue(tartuDistricts.stream().allMatch(district -> district.getCity().getId() == 2));
        assertTrue(parnuDistricts.stream().allMatch(district -> district.getCity().getId() == 3));
    }

    @Test
    void existingCityWithoutDistrictsProducesEmptyList() {
        City city = new City();
        city.setCityName("Linnaosadeta testlinn");
        City savedCity = cityRepository.saveAndFlush(city);

        assertTrue(districtRepository.findDistrictsBy(savedCity.getId()).isEmpty());
    }

    @Test
    void missingCityProducesEmptyList() {
        assertTrue(districtRepository.findDistrictsBy(123).isEmpty());
    }
}
