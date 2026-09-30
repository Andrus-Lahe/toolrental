package ee.toolrental.service;

import ee.toolrental.controller.district.dto.DistrictDto;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.toolrental.persistence.district.District;
import ee.toolrental.persistence.district.DistrictMapper;
import ee.toolrental.persistence.district.DistrictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class DistrictService {

    private static final String DISTRICTS_LOADING_FAILED = "Linnaosade laadimine ebaõnnestus. Palun proovi hiljem uuesti.";

    private final DistrictRepository districtRepository;
    private final DistrictMapper districtMapper;
    private final CityService cityService;

    /**
     * Tagastab valitud linna linnaosad järjestuses district.id ASC.
     * Kui linna pole, visatakse PrimaryKeyNotFoundException (404); olemasoleva linna tühi loend annab [].
     * Andmebaasi tõrge muudetakse 500 vastuseks.
     */
    @Transactional(readOnly = true)
    public List<DistrictDto> getDistricts(Integer cityId) {
        try {
            cityService.getValidCityBy(cityId);
            List<District> districts = districtRepository.findDistrictsBy(cityId);
            return districtMapper.toDistrictDtos(districts);
        } catch (DataAccessException e) {
            throw new InternalServerErrorException(DISTRICTS_LOADING_FAILED);
        }
    }

    /**
     * Leiab linnaosa ID järgi.
     * Kui linnaosa pole, visatakse PrimaryKeyNotFoundException (404 PRIMARY_KEY_NOT_FOUND).
     */
    public District getValidDistrictBy(Integer districtId) {
        return districtRepository.findById(districtId).orElseThrow(() -> new PrimaryKeyNotFoundException("districtId", districtId));
    }

}
