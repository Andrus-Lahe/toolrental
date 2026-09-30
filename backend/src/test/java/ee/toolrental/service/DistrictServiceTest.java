package ee.toolrental.service;

import ee.toolrental.controller.district.dto.DistrictDto;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.toolrental.persistence.city.City;
import ee.toolrental.persistence.district.District;
import ee.toolrental.persistence.district.DistrictMapper;
import ee.toolrental.persistence.district.DistrictRepository;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DistrictServiceTest {

    private final DistrictRepository districtRepository = mock(DistrictRepository.class);
    private final DistrictMapper districtMapper = Mappers.getMapper(DistrictMapper.class);
    private final CityService cityService = mock(CityService.class);
    private final DistrictService districtService = new DistrictService(districtRepository, districtMapper, cityService);

    @Test
    void returnsDistrictDtosOfSelectedCity() {
        when(cityService.getValidCityBy(1)).thenReturn(new City());
        when(districtRepository.findDistrictsBy(1)).thenReturn(List.of(district(1, "Kristiine"), district(2, "Mustamäe")));

        List<DistrictDto> result = districtService.getDistricts(1);

        assertEquals(List.of(new DistrictDto(1, "Kristiine"), new DistrictDto(2, "Mustamäe")), result);
    }

    @Test
    void existingCityWithoutDistrictsProducesEmptyList() {
        when(cityService.getValidCityBy(4)).thenReturn(new City());
        when(districtRepository.findDistrictsBy(4)).thenReturn(List.of());

        assertTrue(districtService.getDistricts(4).isEmpty());
    }

    @Test
    void missingCityProducesPrimaryKeyNotFound() {
        when(cityService.getValidCityBy(123)).thenThrow(new PrimaryKeyNotFoundException("cityId", 123));

        PrimaryKeyNotFoundException exception = assertThrows(
                PrimaryKeyNotFoundException.class, () -> districtService.getDistricts(123));

        assertEquals("Ei leidnud primary keyd 'cityId' väärtusega: 123", exception.getMessage());
        verify(districtRepository, never()).findDistrictsBy(123);
    }

    @Test
    void databaseFailureProducesInternalServerErrorWithoutTechnicalDetails() {
        when(cityService.getValidCityBy(1)).thenReturn(new City());
        when(districtRepository.findDistrictsBy(1)).thenThrow(new DataAccessResourceFailureException("SQL details"));

        InternalServerErrorException exception = assertThrows(
                InternalServerErrorException.class, () -> districtService.getDistricts(1));

        assertEquals("INTERNAL_SERVER_ERROR", exception.getErrorCode());
        assertEquals("Linnaosade laadimine ebaõnnestus. Palun proovi hiljem uuesti.", exception.getMessage());
        assertFalse(exception.getMessage().contains("SQL details"));
    }

    private District district(int id, String name) {
        District district = new District();
        district.setId(id);
        district.setDistrictName(name);
        return district;
    }
}
