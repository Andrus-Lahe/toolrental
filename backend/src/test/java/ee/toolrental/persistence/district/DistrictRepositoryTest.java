package ee.toolrental.persistence.district;

import ee.toolrental.persistence.city.City;
import ee.toolrental.persistence.city.CityRepository;
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
class DistrictRepositoryTest {

    @Autowired
    private DistrictRepository districtRepository;

    @Autowired
    private CityRepository cityRepository;

    @Test
    void returnsOnlySelectedCityDistrictsOrderedById() {
        City tallinn = cityRepository.saveAndFlush(city("Tallinn"));
        City tartu = cityRepository.saveAndFlush(city("Tartu"));
        District kristiine = districtRepository.saveAndFlush(district(tallinn, "Kristiine"));
        districtRepository.saveAndFlush(district(tartu, "Annelinn"));
        District mustamae = districtRepository.saveAndFlush(district(tallinn, "Mustamäe"));

        List<Integer> ids = districtRepository.findDistrictsBy(tallinn.getId()).stream()
                .map(District::getId)
                .toList();

        assertEquals(List.of(kristiine.getId(), mustamae.getId()), ids);
    }

    @Test
    void cityWithoutDistrictsProducesEmptyList() {
        City parnu = cityRepository.saveAndFlush(city("Pärnu"));

        assertTrue(districtRepository.findDistrictsBy(parnu.getId()).isEmpty());
    }

    private City city(String name) {
        City city = new City();
        city.setCityName(name);
        return city;
    }

    private District district(City city, String name) {
        District district = new District();
        district.setCity(city);
        district.setDistrictName(name);
        return district;
    }
}
