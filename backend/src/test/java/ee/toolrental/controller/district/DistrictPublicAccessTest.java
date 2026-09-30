package ee.toolrental.controller.district;

import ee.toolrental.infrastructure.security.DevSecurityConfig;
import ee.toolrental.service.DistrictService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DistrictController.class)
@Import(DevSecurityConfig.class)
@ActiveProfiles("dev-no-auth")
@EnableWebSecurity
class DistrictPublicAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DistrictService districtService;

    @Test
    void guestCanReadCityDistricts() throws Exception {
        when(districtService.getDistricts(1)).thenReturn(List.of());

        mockMvc.perform(get("/api/cities/1/districts"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
