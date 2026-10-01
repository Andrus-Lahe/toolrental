package ee.toolrental.service;

import ee.toolrental.controller.tool.dto.ToolCreateRequestDto;
import ee.toolrental.controller.tool.dto.ToolListItemDto;
import ee.toolrental.controller.tool.dto.ToolsResponse;
import ee.toolrental.infrastructure.exception.ForbiddenException;
import ee.toolrental.infrastructure.exception.IncorrectInputException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.persistence.profile.ProfileRepository;
import ee.toolrental.persistence.tool.Tool;
import ee.toolrental.persistence.tool.ToolListRow;
import ee.toolrental.persistence.tool.ToolMapper;
import ee.toolrental.persistence.tool.ToolRepository;
import ee.toolrental.persistence.toolimage.ToolImage;
import ee.toolrental.persistence.toolimage.ToolImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ToolService {
    private static final String SAVE_FAILED = "Tööriista lisamine ebaõnnestus. Palun proovi hiljem uuesti.";
    private static final String TOOLS_LOADING_FAILED = "Tööriistade laadimine ebaõnnestus. Palun proovi hiljem uuesti.";
    private static final Set<String> ALLOWED_STATUSES = Set.of("A", "U", "0");
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
            log.error("Tööriista lisamine ebaõnnestus (ownerId={}, categoryId={})", ownerId, request.getCategoryId(), exception);
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

    @Transactional(readOnly = true)
    public ToolsResponse getTools(String categoryId, String cityId, String districtId, String status, String pageNumber, String pageSize) {
        int validCategoryId = getValidIdFilter("categoryId", categoryId);
        int validCityId = getValidIdFilter("cityId", cityId);
        int validDistrictId = getValidIdFilter("districtId", districtId);
        String validStatus = getValidStatus(status);
        int validPageNumber = getValidPageValue("pageNumber", pageNumber, 1);
        int validPageSize = getValidPageValue("pageSize", pageSize, 12);
        try {
            if ((long) (validPageNumber - 1) * validPageSize > Integer.MAX_VALUE) {
                Page<ToolListRow> firstPage = toolRepository.findToolListRowsBy(validCategoryId, validCityId, validDistrictId, validStatus, PageRequest.of(0, validPageSize));
                return new ToolsResponse(validPageNumber, validPageSize, firstPage.getTotalPages(), firstPage.getTotalElements(), List.of());
            }
            Page<ToolListRow> page = toolRepository.findToolListRowsBy(validCategoryId, validCityId, validDistrictId, validStatus, PageRequest.of(validPageNumber - 1, validPageSize));
            List<ToolListItemDto> items = toolMapper.toToolListItemDtos(page.getContent());
            return new ToolsResponse(validPageNumber, validPageSize, page.getTotalPages(), page.getTotalElements(), items);
        } catch (DataAccessException exception) {
            log.error("Tööriistade laadimine ebaõnnestus", exception);
            throw new InternalServerErrorException(TOOLS_LOADING_FAILED);
        }
    }

    private int getValidIdFilter(String name, String value) {
        if (value == null) return 0;
        int result = parseInteger(name, value);
        if (result < 0) throw new IncorrectInputException(name + ": peab olema 0 või positiivne täisarv");
        return result;
    }
    private String getValidStatus(String status) {
        if (status == null) return "A";
        if (!ALLOWED_STATUSES.contains(status)) throw new IncorrectInputException("status: lubatud väärtused on A, U ja 0");
        return status;
    }
    private int getValidPageValue(String name, String value, int defaultValue) {
        if (value == null) return defaultValue;
        int result = parseInteger(name, value);
        if (result < 1) throw new IncorrectInputException(name + ": peab olema vähemalt 1");
        return result;
    }
    private int parseInteger(String name, String value) {
        try { return Integer.parseInt(value); }
        catch (NumberFormatException exception) { throw new IncorrectInputException(name + ": peab olema Integer-tüüpi täisarv"); }
    }
}
