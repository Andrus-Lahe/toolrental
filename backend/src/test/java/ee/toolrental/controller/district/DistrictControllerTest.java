package ee.toolrental.controller.district;

import ee.toolrental.controller.district.dto.DistrictDto;
import ee.toolrental.infrastructure.RestExceptionHandler;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.toolrental.service.DistrictService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DistrictControllerTest {

    private final DistrictService districtService = mock(DistrictService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new DistrictController(districtService))
                .setControllerAdvice(new RestExceptionHandler())
                .build();
    }

    @Test
    void returnsDistrictsWithExpectedShape() throws Exception {
        when(districtService.getDistricts(1)).thenReturn(List.of(
                new DistrictDto(1, "Kristiine"),
                new DistrictDto(2, "Mustamäe")));

        mockMvc.perform(get("/api/cities/1/districts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].districtId").value(1))
                .andExpect(jsonPath("$[0].districtName").value("Kristiine"))
                .andExpect(jsonPath("$[0].length()").value(2))
                .andExpect(jsonPath("$[1].districtId").value(2));
    }

    @Test
    void existingCityWithoutDistrictsProducesOkWithEmptyArray() throws Exception {
        when(districtService.getDistricts(4)).thenReturn(List.of());

        mockMvc.perform(get("/api/cities/4/districts"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void missingCityProducesNotFound() throws Exception {
        when(districtService.getDistricts(123)).thenThrow(new PrimaryKeyNotFoundException("cityId", 123));

        mockMvc.perform(get("/api/cities/123/districts"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRIMARY_KEY_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Ei leidnud primary keyd 'cityId' väärtusega: 123"));
    }

    @Test
    void nonIntegerCityIdProducesBadRequest() throws Exception {
        mockMvc.perform(get("/api/cities/abc/districts"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INCORRECT_INPUT"))
                .andExpect(jsonPath("$.message").value("cityId: peab olema Integer-tüüpi täisarv"));
    }

    @Test
    void cityIdOutOfIntegerRangeProducesBadRequest() throws Exception {
        mockMvc.perform(get("/api/cities/99999999999/districts"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INCORRECT_INPUT"))
                .andExpect(jsonPath("$.message").value("cityId: peab olema Integer-tüüpi täisarv"));
    }

    @Test
    void loadingFailureProducesSpecifiedApiError() throws Exception {
        when(districtService.getDistricts(1)).thenThrow(new InternalServerErrorException(
                "Linnaosade laadimine ebaõnnestus. Palun proovi hiljem uuesti."));

        mockMvc.perform(get("/api/cities/1/districts"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value(
                        "Linnaosade laadimine ebaõnnestus. Palun proovi hiljem uuesti."));
    }
}
