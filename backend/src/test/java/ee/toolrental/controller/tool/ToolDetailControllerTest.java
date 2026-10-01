package ee.toolrental.controller.tool;

import ee.toolrental.controller.tool.dto.ToolDetailResponse;
import ee.toolrental.infrastructure.RestExceptionHandler;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.toolrental.service.ToolService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ToolDetailControllerTest {

    private final ToolService toolService = mock(ToolService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ToolController(toolService))
                .setControllerAdvice(new RestExceptionHandler())
                .build();
    }

    @Test
    void returnsToolDetailResponseWithExpectedShape() throws Exception {
        when(toolService.getToolDetail(1)).thenReturn(new ToolDetailResponse(
                1, 1, "Akutrell", "Ehitustööd", "18 V akutrell", "PHN2Zy8+", "A"));

        mockMvc.perform(get("/api/tools/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(7))
                .andExpect(jsonPath("$.toolId").value(1))
                .andExpect(jsonPath("$.ownerId").value(1))
                .andExpect(jsonPath("$.toolName").value("Akutrell"))
                .andExpect(jsonPath("$.categoryName").value("Ehitustööd"))
                .andExpect(jsonPath("$.description").value("18 V akutrell"))
                .andExpect(jsonPath("$.imageData").value("PHN2Zy8+"))
                .andExpect(jsonPath("$.status").value("A"));
    }

    @Test
    void nullDescriptionAndImageDataAreIncluded() throws Exception {
        when(toolService.getToolDetail(9)).thenReturn(new ToolDetailResponse(
                9, 3, "Pildita", "Muud", null, null, "A"));

        mockMvc.perform(get("/api/tools/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(7))
                .andExpect(jsonPath("$.description").value(nullValue()))
                .andExpect(jsonPath("$.imageData").value(nullValue()));
    }

    @Test
    void unavailableToolIsReturned() throws Exception {
        when(toolService.getToolDetail(2)).thenReturn(new ToolDetailResponse(
                2, 1, "Redel", "Ehitustööd", null, "PHN2Zy8+", "U"));

        mockMvc.perform(get("/api/tools/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("U"));
    }

    @Test
    void missingToolProducesNotFound() throws Exception {
        when(toolService.getToolDetail(123)).thenThrow(new PrimaryKeyNotFoundException("toolId", 123));

        mockMvc.perform(get("/api/tools/123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRIMARY_KEY_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Ei leidnud primary keyd 'toolId' väärtusega: 123"));
    }

    @Test
    void nonIntegerToolIdProducesBadRequest() throws Exception {
        mockMvc.perform(get("/api/tools/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INCORRECT_INPUT"))
                .andExpect(jsonPath("$.message").value("toolId: peab olema Integer-tüüpi täisarv"));

        verifyNoInteractions(toolService);
    }

    @Test
    void databaseFailureProducesInternalServerError() throws Exception {
        when(toolService.getToolDetail(1))
                .thenThrow(new InternalServerErrorException("Tööriista laadimine ebaõnnestus. Palun proovi hiljem uuesti."));

        mockMvc.perform(get("/api/tools/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("Tööriista laadimine ebaõnnestus. Palun proovi hiljem uuesti."));
    }
}
