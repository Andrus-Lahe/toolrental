package ee.toolrental.persistence.tool;

import ee.toolrental.controller.common.dto.MyToolCardDto;
import ee.toolrental.controller.tool.dto.ToolCreateRequestDto;
import ee.toolrental.controller.tool.dto.ToolDetailResponse;
import ee.toolrental.controller.tool.dto.ToolListItemDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.Base64;
import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ToolMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Tool toTool(ToolCreateRequestDto request);

    @Mapping(target = "toolId", source = "tool.id")
    @Mapping(target = "toolName", source = "tool.name")
    @Mapping(target = "toolStatus", source = "tool.status")
    @Mapping(target = "imageData", expression = "java(imageData == null ? null : java.util.Base64.getEncoder().encodeToString(imageData))")
    MyToolCardDto toMyToolCardDto(Tool tool, byte[] imageData);

    @Mapping(target = "imageData", expression = "java(toBase64(toolListRow.imageData()))")
    ToolListItemDto toToolListItemDto(ToolListRow toolListRow);

    List<ToolListItemDto> toToolListItemDtos(List<ToolListRow> toolListRows);

    @Mapping(target = "toolId", source = "tool.id")
    @Mapping(target = "ownerId", source = "tool.owner.id")
    @Mapping(target = "toolName", source = "tool.name")
    @Mapping(target = "categoryName", source = "tool.category.categoryName")
    @Mapping(target = "description", source = "tool.description")
    @Mapping(target = "imageData", expression = "java(toBase64(imageData))")
    @Mapping(target = "status", source = "tool.status")
    ToolDetailResponse toToolDetailResponse(Tool tool, byte[] imageData);

    default String toBase64(byte[] imageData) {
        return imageData == null ? null : Base64.getEncoder().encodeToString(imageData);
    }
}
