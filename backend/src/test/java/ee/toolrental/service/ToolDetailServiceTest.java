package ee.toolrental.service;

import ee.toolrental.controller.tool.dto.ToolDetailResponse;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.toolrental.persistence.appuser.AppUser;
import ee.toolrental.persistence.category.Category;
import ee.toolrental.persistence.profile.ProfileRepository;
import ee.toolrental.persistence.tool.Tool;
import ee.toolrental.persistence.tool.ToolMapper;
import ee.toolrental.persistence.tool.ToolRepository;
import ee.toolrental.persistence.toolimage.ToolImage;
import ee.toolrental.persistence.toolimage.ToolImageRepository;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.dao.DataAccessResourceFailureException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ToolDetailServiceTest {

    private final ToolRepository toolRepository = mock(ToolRepository.class);
    private final ToolImageRepository toolImageRepository = mock(ToolImageRepository.class);
    private final ToolMapper toolMapper = Mappers.getMapper(ToolMapper.class);
    private final ToolService toolService = new ToolService(toolRepository, toolImageRepository, toolMapper,
            mock(AppUserService.class), mock(CategoryService.class), mock(ProfileRepository.class));

    @Test
    void returnsToolDetailWithMainImageAsBase64() {
        byte[] imageBytes = "<svg>Akutrell</svg>".getBytes(StandardCharsets.UTF_8);
        when(toolRepository.findById(1)).thenReturn(Optional.of(createTool(1, "18 V akutrell", "A")));
        when(toolImageRepository.findMainToolImageBy(1)).thenReturn(Optional.of(createToolImage(imageBytes)));

        ToolDetailResponse toolDetailResponse = toolService.getToolDetail(1);

        assertEquals(1, toolDetailResponse.getToolId());
        assertEquals(7, toolDetailResponse.getOwnerId());
        assertEquals("Akutrell", toolDetailResponse.getToolName());
        assertEquals("Ehitustööd", toolDetailResponse.getCategoryName());
        assertEquals("18 V akutrell", toolDetailResponse.getDescription());
        assertEquals("A", toolDetailResponse.getStatus());
        assertArrayEquals(imageBytes, Base64.getDecoder().decode(toolDetailResponse.getImageData()));
    }

    @Test
    void missingMainImageGivesNullImageData() {
        when(toolRepository.findById(1)).thenReturn(Optional.of(createTool(1, "18 V akutrell", "A")));
        when(toolImageRepository.findMainToolImageBy(1)).thenReturn(Optional.empty());

        ToolDetailResponse toolDetailResponse = toolService.getToolDetail(1);

        assertNull(toolDetailResponse.getImageData());
    }

    @Test
    void nullDescriptionStaysNull() {
        when(toolRepository.findById(1)).thenReturn(Optional.of(createTool(1, null, "A")));
        when(toolImageRepository.findMainToolImageBy(1)).thenReturn(Optional.empty());

        ToolDetailResponse toolDetailResponse = toolService.getToolDetail(1);

        assertNull(toolDetailResponse.getDescription());
    }

    @Test
    void unavailableToolIsReturned() {
        when(toolRepository.findById(2)).thenReturn(Optional.of(createTool(2, null, "U")));
        when(toolImageRepository.findMainToolImageBy(2)).thenReturn(Optional.empty());

        ToolDetailResponse toolDetailResponse = toolService.getToolDetail(2);

        assertEquals("U", toolDetailResponse.getStatus());
    }

    @Test
    void missingToolThrowsPrimaryKeyNotFound() {
        when(toolRepository.findById(123)).thenReturn(Optional.empty());

        PrimaryKeyNotFoundException exception = assertThrows(PrimaryKeyNotFoundException.class,
                () -> toolService.getToolDetail(123));

        assertEquals("Ei leidnud primary keyd 'toolId' väärtusega: 123", exception.getMessage());
        verifyNoInteractions(toolImageRepository);
    }

    @Test
    void databaseFailureThrowsInternalServerError() {
        when(toolRepository.findById(1)).thenThrow(new DataAccessResourceFailureException("db down"));

        InternalServerErrorException exception = assertThrows(InternalServerErrorException.class,
                () -> toolService.getToolDetail(1));

        assertEquals("Tööriista laadimine ebaõnnestus. Palun proovi hiljem uuesti.", exception.getMessage());
    }

    @Test
    void readingToolDetailDoesNotSaveData() {
        when(toolRepository.findById(1)).thenReturn(Optional.of(createTool(1, null, "A")));
        when(toolImageRepository.findMainToolImageBy(1)).thenReturn(Optional.empty());

        toolService.getToolDetail(1);

        verify(toolRepository, never()).saveAndFlush(any());
        verify(toolRepository, never()).save(any());
        verify(toolImageRepository, never()).saveAndFlush(any());
        verify(toolImageRepository, never()).save(any());
    }

    private Tool createTool(Integer toolId, String description, String status) {
        AppUser owner = new AppUser();
        owner.setId(7);
        Category category = new Category();
        category.setId(2);
        category.setCategoryName("Ehitustööd");
        Tool tool = new Tool();
        tool.setId(toolId);
        tool.setOwner(owner);
        tool.setCategory(category);
        tool.setName(toolId == 1 ? "Akutrell" : "Redel");
        tool.setDescription(description);
        tool.setStatus(status);
        return tool;
    }

    private ToolImage createToolImage(byte[] imageBytes) {
        ToolImage toolImage = new ToolImage();
        toolImage.setImageData(imageBytes);
        toolImage.setIsMain(true);
        return toolImage;
    }
}
