package ee.toolrental.service;

import ee.toolrental.controller.tool.dto.ToolCreateRequestDto;
import ee.toolrental.infrastructure.exception.ForbiddenException;
import ee.toolrental.infrastructure.exception.IncorrectInputException;
import ee.toolrental.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.toolrental.persistence.appuser.AppUser;
import ee.toolrental.persistence.category.Category;
import ee.toolrental.persistence.profile.ProfileRepository;
import ee.toolrental.persistence.tool.Tool;
import ee.toolrental.persistence.tool.ToolMapper;
import ee.toolrental.persistence.tool.ToolRepository;
import ee.toolrental.persistence.toolimage.ToolImage;
import ee.toolrental.persistence.toolimage.ToolImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ToolServiceTest {
    private ToolRepository toolRepository;
    private ToolImageRepository toolImageRepository;
    private ToolMapper toolMapper;
    private AppUserService appUserService;
    private CategoryService categoryService;
    private ProfileRepository profileRepository;
    private ToolService toolService;

    @BeforeEach
    void setUp() {
        toolRepository = mock(ToolRepository.class);
        toolImageRepository = mock(ToolImageRepository.class);
        toolMapper = mock(ToolMapper.class);
        appUserService = mock(AppUserService.class);
        categoryService = mock(CategoryService.class);
        profileRepository = mock(ProfileRepository.class);
        toolService = new ToolService(toolRepository, toolImageRepository, toolMapper,
                appUserService, categoryService, profileRepository);
        when(profileRepository.findProfileBy(3)).thenReturn(Optional.of(mock(ee.toolrental.persistence.profile.Profile.class)));
        when(appUserService.getValidAppUserBy(3)).thenReturn(new AppUser());
        when(categoryService.getValidCategoryBy(2)).thenReturn(new Category());
        when(toolMapper.toTool(any())).thenAnswer(invocation -> {
            ToolCreateRequestDto request = invocation.getArgument(0);
            Tool tool = new Tool();
            tool.setName(request.getName());
            tool.setDescription(request.getDescription());
            return tool;
        });
        when(toolRepository.saveAndFlush(any(Tool.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createToolSavesDecodedMainImageAndUsesAuthenticatedOwner() {
        ToolCreateRequestDto request = request("c3ZnLWJ5dGVz", "Akutrell", "Kirjeldus");

        toolService.createTool(3, request);

        verify(toolMapper).toTool(request);
        verify(appUserService).getValidAppUserBy(3);
        verify(toolRepository).saveAndFlush(argThat(tool -> tool.getOwner() != null
                && tool.getCategory() != null && "A".equals(tool.getStatus())
                && "Akutrell".equals(tool.getName()) && "Kirjeldus".equals(tool.getDescription())
                && tool.getCreatedAt() != null && tool.getUpdatedAt() != null));
        verify(toolImageRepository).saveAndFlush(argThat(image -> image.isMain()
                && image.getTool() != null && new String(image.getImageData()).equals("svg-bytes")));
    }

    @Test
    void createToolAllowsNullDescriptionAndDoesNotSaveEmptyImage() {
        ToolCreateRequestDto request = request("", "Tööriist", null);

        toolService.createTool(3, request);

        verify(toolImageRepository, never()).saveAndFlush(any(ToolImage.class));
        verify(toolMapper).toTool(request);
    }

    @Test
    void createToolRejectsMalformedBase64BeforePersistence() {
        ToolCreateRequestDto request = request("not base64!", "Tööriist", null);

        IncorrectInputException exception = assertThrows(IncorrectInputException.class,
                () -> toolService.createTool(3, request));

        assertEquals("imageData: peab olema korrektne Base64", exception.getMessage());
        verifyNoInteractions(profileRepository, toolRepository, toolImageRepository);
    }

    @Test
    void createToolRequiresProfile() {
        when(profileRepository.findProfileBy(3)).thenReturn(Optional.empty());

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> toolService.createTool(3, request("", "Tööriist", null)));

        assertEquals("PROFILE_REQUIRED", exception.getErrorCode());
        verify(toolRepository, never()).saveAndFlush(any());
    }

    @Test
    void createToolRejectsMissingCategoryBeforeSaving() {
        when(categoryService.getValidCategoryBy(2)).thenThrow(new PrimaryKeyNotFoundException("categoryId", 2));

        PrimaryKeyNotFoundException exception = assertThrows(PrimaryKeyNotFoundException.class,
                () -> toolService.createTool(3, request("", "Tööriist", null)));

        assertEquals("Ei leidnud primary keyd 'categoryId' väärtusega: 2", exception.getMessage());
        verify(toolRepository, never()).saveAndFlush(any());
        verifyNoInteractions(toolImageRepository);
    }

    @Test
    void createToolMapsDatabaseFailureToTaskError() {
        when(toolRepository.saveAndFlush(any(Tool.class)))
                .thenThrow(new DataAccessResourceFailureException("database unavailable"));

        var exception = assertThrows(ee.toolrental.infrastructure.exception.InternalServerErrorException.class,
                () -> toolService.createTool(3, request("", "Tööriist", null)));

        assertEquals("Tööriista lisamine ebaõnnestus. Palun proovi hiljem uuesti.", exception.getMessage());
    }

    private ToolCreateRequestDto request(String imageData, String name, String description) {
        ToolCreateRequestDto request = new ToolCreateRequestDto();
        request.setCategoryId(2);
        request.setName(name);
        request.setDescription(description);
        request.setImageData(imageData);
        return request;
    }
}
