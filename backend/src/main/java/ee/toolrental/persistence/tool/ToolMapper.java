package ee.toolrental.persistence.tool;

import ee.toolrental.controller.tool.dto.ToolListItemDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.Base64;
import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ToolMapper {
    ToolListItemDto toToolListItemDto(ToolListRow toolListRow);

    List<ToolListItemDto> toToolListItemDtos(List<ToolListRow> toolListRows);

    default String toBase64(byte[] imageData) {
        return imageData == null ? null : Base64.getEncoder().encodeToString(imageData);
    }
}
