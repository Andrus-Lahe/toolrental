package ee.toolrental.service;

import ee.toolrental.controller.city.dto.CityDto;
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
}
