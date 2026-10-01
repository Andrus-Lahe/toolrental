package ee.toolrental.service;

import ee.toolrental.controller.tool.dto.ToolListItemDto;
import ee.toolrental.controller.tool.dto.ToolsResponse;
import ee.toolrental.infrastructure.exception.IncorrectInputException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.persistence.tool.ToolListRow;
import ee.toolrental.persistence.tool.ToolMapper;
import ee.toolrental.persistence.tool.ToolRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ToolListServiceTest {

    private final ToolRepository toolRepository = mock(ToolRepository.class);
    private final ToolMapper toolMapper = Mappers.getMapper(ToolMapper.class);
    private final ToolService toolService = new ToolService(toolRepository, mock(ee.toolrental.persistence.toolimage.ToolImageRepository.class), toolMapper, mock(AppUserService.class), mock(CategoryService.class), mock(ee.toolrental.persistence.profile.ProfileRepository.class));

    @Test
    void missingParametersUseDefaultValues() {
        Pageable pageable = PageRequest.of(0, 12);
        when(toolRepository.findToolListRowsBy(0, 0, 0, "A", pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        ToolsResponse toolsResponse = toolService.getTools(null, null, null, null, null, null);

        verify(toolRepository).findToolListRowsBy(0, 0, 0, "A", pageable);
        assertEquals(1, toolsResponse.getPageNumber());
        assertEquals(12, toolsResponse.getPageSize());
        assertEquals(0, toolsResponse.getTotalPages());
        assertEquals(0L, toolsResponse.getTotalElements());
        assertTrue(toolsResponse.getTools().isEmpty());
    }

    @Test
    void oneBasedPageNumberIsConvertedToZeroBasedPageable() {
        Pageable pageable = PageRequest.of(1, 6);
        when(toolRepository.findToolListRowsBy(2, 1, 3, "0", pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 7));

        ToolsResponse toolsResponse = toolService.getTools("2", "1", "3", "0", "2", "6");

        verify(toolRepository).findToolListRowsBy(2, 1, 3, "0", pageable);
        assertEquals(2, toolsResponse.getPageNumber());
        assertEquals(6, toolsResponse.getPageSize());
    }

    @Test
    void mapsRowsAndEncodesMainImageAsBase64() {
        byte[] imageData = "<svg/>".getBytes(StandardCharsets.UTF_8);
        Pageable pageable = PageRequest.of(0, 12);
        List<ToolListRow> toolListRows = List.of(
                new ToolListRow(1, "Akutrell", "18 V akutrell", imageData, "A", "Tallinn", "Kristiine"),
                new ToolListRow(9, "Pildita", null, null, "A", null, null));
        when(toolRepository.findToolListRowsBy(0, 0, 0, "A", pageable))
                .thenReturn(new PageImpl<>(toolListRows, pageable, 2));

        ToolsResponse toolsResponse = toolService.getTools(null, null, null, null, null, null);

        assertEquals(new ToolListItemDto(1, "Akutrell", "18 V akutrell",
                Base64.getEncoder().encodeToString(imageData), "A", "Tallinn", "Kristiine"),
                toolsResponse.getTools().get(0));
        ToolListItemDto toolWithoutImage = toolsResponse.getTools().get(1);
        assertNull(toolWithoutImage.getDescription());
        assertNull(toolWithoutImage.getImageData());
        assertNull(toolWithoutImage.getCityName());
        assertNull(toolWithoutImage.getDistrictName());
        assertEquals(1, toolsResponse.getTotalPages());
        assertEquals(2L, toolsResponse.getTotalElements());
    }

    @Test
    void pageAfterLastPageKeepsTotals() {
        Pageable pageable = PageRequest.of(1, 12);
        when(toolRepository.findToolListRowsBy(0, 0, 0, "A", pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 7));

        ToolsResponse toolsResponse = toolService.getTools(null, null, null, null, "2", null);

        assertTrue(toolsResponse.getTools().isEmpty());
        assertEquals(7L, toolsResponse.getTotalElements());
        assertEquals(1, toolsResponse.getTotalPages());
    }

    @Test
    void offsetBeyondIntegerRangeReturnsEmptyPageWithTotalsOfFirstPage() {
        Pageable firstPageable = PageRequest.of(0, 12);
        when(toolRepository.findToolListRowsBy(0, 0, 0, "A", firstPageable))
                .thenReturn(new PageImpl<>(List.of(new ToolListRow(1, "Akutrell", null, null, "A", null, null)),
                        firstPageable, 13));

        ToolsResponse toolsResponse = toolService.getTools(null, null, null, null, "200000000", null);

        verify(toolRepository).findToolListRowsBy(0, 0, 0, "A", firstPageable);
        assertEquals(200000000, toolsResponse.getPageNumber());
        assertTrue(toolsResponse.getTools().isEmpty());
        assertEquals(13L, toolsResponse.getTotalElements());
        assertEquals(2, toolsResponse.getTotalPages());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "abc", "1.5", "99999999999"})
    void nonIntegerValueProducesIncorrectInput(String value) {
        assertIncorrectInput("pageNumber: peab olema Integer-tüüpi täisarv",
                () -> toolService.getTools(null, null, null, null, value, null));
        assertIncorrectInput("categoryId: peab olema Integer-tüüpi täisarv",
                () -> toolService.getTools(value, null, null, null, null, null));
    }

    @Test
    void negativeIdFilterProducesIncorrectInput() {
        assertIncorrectInput("categoryId: peab olema 0 või positiivne täisarv",
                () -> toolService.getTools("-1", null, null, null, null, null));
        assertIncorrectInput("cityId: peab olema 0 või positiivne täisarv",
                () -> toolService.getTools(null, "-1", null, null, null, null));
        assertIncorrectInput("districtId: peab olema 0 või positiivne täisarv",
                () -> toolService.getTools(null, null, "-1", null, null, null));
    }

    @Test
    void pageValueBelowOneProducesIncorrectInput() {
        assertIncorrectInput("pageNumber: peab olema vähemalt 1",
                () -> toolService.getTools(null, null, null, null, "0", null));
        assertIncorrectInput("pageSize: peab olema vähemalt 1",
                () -> toolService.getTools(null, null, null, null, null, "0"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "a", "X", "AU"})
    void unknownStatusProducesIncorrectInput(String status) {
        assertIncorrectInput("status: lubatud väärtused on A, U ja 0",
                () -> toolService.getTools(null, null, null, status, null, null));
    }

    @Test
    void databaseFailureProducesInternalServerError() {
        when(toolRepository.findToolListRowsBy(anyInt(), anyInt(), anyInt(), anyString(), any(Pageable.class)))
                .thenThrow(new DataAccessResourceFailureException("db down"));

        InternalServerErrorException exception = assertThrows(InternalServerErrorException.class,
                () -> toolService.getTools(null, null, null, null, null, null));

        assertEquals("Tööriistade laadimine ebaõnnestus. Palun proovi hiljem uuesti.", exception.getMessage());
        assertEquals("INTERNAL_SERVER_ERROR", exception.getErrorCode());
    }

    private void assertIncorrectInput(String expectedMessage, Runnable toolsRequest) {
        IncorrectInputException exception = assertThrows(IncorrectInputException.class, toolsRequest::run);

        assertEquals(expectedMessage, exception.getMessage());
        assertEquals("INCORRECT_INPUT", exception.getErrorCode());
        verify(toolRepository, never()).findToolListRowsBy(anyInt(), anyInt(), anyInt(), anyString(), any(Pageable.class));
    }
}
