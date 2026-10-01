package ee.toolrental.service;

import ee.toolrental.controller.tool.dto.ToolCreateRequestDto;
import ee.toolrental.infrastructure.exception.ForbiddenException;
import ee.toolrental.infrastructure.exception.IncorrectInputException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.persistence.profile.ProfileRepository;
import ee.toolrental.persistence.tool.Tool;
import ee.toolrental.persistence.tool.ToolMapper;
import ee.toolrental.persistence.tool.ToolRepository;
import ee.toolrental.persistence.toolimage.ToolImage;
import ee.toolrental.persistence.toolimage.ToolImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class ToolService {
    private static final String SAVE_FAILED = "Tööriista lisamine ebaõnnestus. Palun proovi hiljem uuesti.";

    private final ToolRepository toolRepository;
    private final ToolImageRepository toolImageRepository;
    private final ToolMapper toolMapper;
    private final AppUserService appUserService;
    private final CategoryService categoryService;
    private final ProfileRepository profileRepository;

    @Transactional
    public void createTool(Integer ownerId, ToolCreateRequestDto request) {
        byte[] imageBytes = decodeImageData(request.getImageData());
        try {
            if (profileRepository.findProfileBy(ownerId).isEmpty()) {
                throw new ForbiddenException("Enne tööriista lisamist täida oma profiil", "PROFILE_REQUIRED");
            }
            Tool tool = toolMapper.toTool(request);
            tool.setOwner(appUserService.getValidAppUserBy(ownerId));
            tool.setCategory(categoryService.getValidCategoryBy(request.getCategoryId()));
            tool.setStatus("A");
            Instant now = Instant.now();
            tool.setCreatedAt(now);
            tool.setUpdatedAt(now);
            tool = toolRepository.saveAndFlush(tool);

            if (imageBytes.length > 0) {
                ToolImage toolImage = new ToolImage();
                toolImage.setTool(tool);
                toolImage.setImageData(imageBytes);
                toolImage.setMain(true);
                toolImageRepository.saveAndFlush(toolImage);
            }
        } catch (DataAccessException exception) {
            throw new InternalServerErrorException(SAVE_FAILED);
        }
    }

    private byte[] decodeImageData(String imageData) {
        try {
            return imageData.isEmpty() ? new byte[0] : Base64.getDecoder().decode(imageData);
        } catch (IllegalArgumentException exception) {
            throw new IncorrectInputException("imageData: peab olema korrektne Base64");
        }
    }
}
