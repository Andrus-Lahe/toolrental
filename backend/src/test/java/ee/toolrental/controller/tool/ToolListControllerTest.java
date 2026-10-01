package ee.toolrental.controller.tool;

import ee.toolrental.controller.tool.dto.ToolListItemDto;
import ee.toolrental.controller.tool.dto.ToolsResponse;
import ee.toolrental.infrastructure.RestExceptionHandler;
import ee.toolrental.infrastructure.exception.IncorrectInputException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.service.ToolService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ToolListControllerTest {

    private final ToolService toolService = mock(ToolService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ToolListController(toolService))
                .setControllerAdvice(new RestExceptionHandler())
                .build();
    }

    @Test
    void returnsToolsResponseWithExpectedShape() throws Exception {
        when(toolService.getTools(null, null, null, null, null, null)).thenReturn(new ToolsResponse(1, 12, 1, 2L, List.of(
                new ToolListItemDto(1, "Akutrell", "18 V akutrell", "PHN2Zy8+", "A", "Tallinn", "Kristiine"),
                new ToolListItemDto(9, "Pildita", null, null, "U", null, null))));

        mockMvc.perform(get("/api/tools"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.pageSize").value(12))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.tools.length()").value(2))
                .andExpect(jsonPath("$.tools[0].length()").value(7))
                .andExpect(jsonPath("$.tools[0].toolId").value(1))
                .andExpect(jsonPath("$.tools[0].toolName").value("Akutrell"))
                .andExpect(jsonPath("$.tools[0].description").value("18 V akutrell"))
                .andExpect(jsonPath("$.tools[0].imageData").value("PHN2Zy8+"))
                .andExpect(jsonPath("$.tools[0].status").value("A"))
                .andExpect(jsonPath("$.tools[0].cityName").value("Tallinn"))
                .andExpect(jsonPath("$.tools[0].districtName").value("Kristiine"))
                .andExpect(jsonPath("$.tools[1].length()").value(7))
                .andExpect(jsonPath("$.tools[1].description").value(nullValue()))
                .andExpect(jsonPath("$.tools[1].imageData").value(nullValue()))
                .andExpect(jsonPath("$.tools[1].cityName").value(nullValue()))
                .andExpect(jsonPath("$.tools[1].districtName").value(nullValue()));
    }

    @Test
    void passesQueryParametersToServiceUnchanged() throws Exception {
        when(toolService.getTools("2", "1", "", "0", "3", "6")).thenReturn(new ToolsResponse(3, 6, 0, 0L, List.of()));

        mockMvc.perform(get("/api/tools?categoryId=2&cityId=1&districtId=&status=0&pageNumber=3&pageSize=6"))
                .andExpect(status().isOk());

        verify(toolService).getTools("2", "1", "", "0", "3", "6");
    }

    @Test
    void emptyResultProducesOkWithEmptyTools() throws Exception {
        when(toolService.getTools(null, "2", null, "0", null, null)).thenReturn(new ToolsResponse(1, 12, 0, 0L, List.of()));

        mockMvc.perform(get("/api/tools?cityId=2&status=0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.tools.length()").value(0));
    }

    @Test
    void incorrectInputProducesBadRequest() throws Exception {
        when(toolService.getTools(null, null, null, null, "", null))
                .thenThrow(new IncorrectInputException("pageNumber: peab olema Integer-tüüpi täisarv"));

        mockMvc.perform(get("/api/tools?pageNumber="))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INCORRECT_INPUT"))
                .andExpect(jsonPath("$.message").value("pageNumber: peab olema Integer-tüüpi täisarv"));
    }

    @Test
    void databaseFailureProducesInternalServerError() throws Exception {
        when(toolService.getTools(null, null, null, null, null, null))
                .thenThrow(new InternalServerErrorException("Tööriistade laadimine ebaõnnestus. Palun proovi hiljem uuesti."));

        mockMvc.perform(get("/api/tools"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("Tööriistade laadimine ebaõnnestus. Palun proovi hiljem uuesti."));
    }
}
