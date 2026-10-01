package ee.toolrental.service;

import ee.toolrental.controller.tool.dto.ToolListItemDto;
import ee.toolrental.controller.tool.dto.ToolsResponse;
import ee.toolrental.infrastructure.exception.IncorrectInputException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.persistence.tool.ToolListRow;
import ee.toolrental.persistence.tool.ToolMapper;
import ee.toolrental.persistence.tool.ToolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class ToolService {

    private static final String TOOLS_LOADING_FAILED = "Tööriistade laadimine ebaõnnestus. Palun proovi hiljem uuesti.";
    private static final Integer NO_ID_FILTER = 0;
    private static final String DEFAULT_STATUS = "A";
    private static final Set<String> ALLOWED_STATUSES = Set.of("A", "U", "0");
    private static final Integer DEFAULT_PAGE_NUMBER = 1;
    private static final Integer DEFAULT_PAGE_SIZE = 12;

    private final ToolRepository toolRepository;
    private final ToolMapper toolMapper;

    /**
     * Tagastab filtritele vastavad tööriistad järjestuses tool.id ASC koos lehekülje metaandmetega.
     * Puuduv parameeter (null) saab vaikeväärtuse, tühi või vigane väärtus annab IncorrectInputException (400).
     * Viimasest lehest suurem lehenumber annab tühja tools loendi ja tegelikud koguarvud.
     * Andmebaasi tõrge muudetakse 500 vastuseks.
     */
    @Transactional(readOnly = true)
    public ToolsResponse getTools(String categoryId, String cityId, String districtId,
                                  String status, String pageNumber, String pageSize) {
        Integer validCategoryId = getValidIdFilter("categoryId", categoryId);
        Integer validCityId = getValidIdFilter("cityId", cityId);
        Integer validDistrictId = getValidIdFilter("districtId", districtId);
        String validStatus = getValidStatus(status);
        Integer validPageNumber = getValidPageValue("pageNumber", pageNumber, DEFAULT_PAGE_NUMBER);
        Integer validPageSize = getValidPageValue("pageSize", pageSize, DEFAULT_PAGE_SIZE);

        try {
            if (isOffsetTooLarge(validPageNumber, validPageSize)) {
                Page<ToolListRow> firstToolListRowPage = toolRepository.findToolListRowsBy(validCategoryId,
                        validCityId, validDistrictId, validStatus, PageRequest.of(0, validPageSize));
                return new ToolsResponse(validPageNumber, validPageSize, firstToolListRowPage.getTotalPages(),
                        firstToolListRowPage.getTotalElements(), List.of());
            }

            Page<ToolListRow> toolListRowPage = toolRepository.findToolListRowsBy(validCategoryId, validCityId,
                    validDistrictId, validStatus, PageRequest.of(validPageNumber - 1, validPageSize));
            List<ToolListItemDto> toolListItemDtos = toolMapper.toToolListItemDtos(toolListRowPage.getContent());
            return new ToolsResponse(validPageNumber, validPageSize, toolListRowPage.getTotalPages(),
                    toolListRowPage.getTotalElements(), toolListItemDtos);
        } catch (DataAccessException e) {
            throw new InternalServerErrorException(TOOLS_LOADING_FAILED);
        }
    }

    private Integer getValidIdFilter(String parameterName, String value) {
        if (value == null) {
            return NO_ID_FILTER;
        }
        Integer idFilter = getParsedInteger(parameterName, value);
        if (idFilter < 0) {
            throw new IncorrectInputException(parameterName + ": peab olema 0 või positiivne täisarv");
        }
        return idFilter;
    }

    private String getValidStatus(String status) {
        if (status == null) {
            return DEFAULT_STATUS;
        }
        if (!ALLOWED_STATUSES.contains(status)) {
            throw new IncorrectInputException("status: lubatud väärtused on A, U ja 0");
        }
        return status;
    }

    private Integer getValidPageValue(String parameterName, String value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        Integer pageValue = getParsedInteger(parameterName, value);
        if (pageValue < 1) {
            throw new IncorrectInputException(parameterName + ": peab olema vähemalt 1");
        }
        return pageValue;
    }

    private Integer getParsedInteger(String parameterName, String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IncorrectInputException(parameterName + ": peab olema Integer-tüüpi täisarv");
        }
    }

    private boolean isOffsetTooLarge(Integer pageNumber, Integer pageSize) {
        return (long) (pageNumber - 1) * pageSize > Integer.MAX_VALUE;
    }

}
