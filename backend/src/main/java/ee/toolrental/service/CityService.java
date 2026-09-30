package ee.toolrental.service;

import ee.toolrental.controller.city.dto.CityDto;
import ee.toolrental.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.toolrental.persistence.city.City;
import ee.toolrental.persistence.city.CityMapper;
import ee.toolrental.persistence.city.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CityService {
    private final CityRepository cityRepository;
    private final CityMapper cityMapper;

    public List<CityDto> getCities() {
        List<City> allCities = cityRepository.findAllCities();
        List<CityDto> cityDtos = cityMapper.toCityDtos(allCities);
        return cityDtos;
    }

    /**
     * Leiab linna ID järgi.
     * Kui linna pole, visatakse PrimaryKeyNotFoundException (404 PRIMARY_KEY_NOT_FOUND).
     */
    public City getValidCityBy(Integer cityId) {
        return cityRepository.findById(cityId).orElseThrow(() -> new PrimaryKeyNotFoundException("cityId", cityId));
    }
}
