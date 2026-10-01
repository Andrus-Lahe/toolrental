package ee.toolrental.persistence.tool;

import ee.toolrental.controller.tool.dto.ToolCreateRequestDto;
import ee.toolrental.controller.common.dto.MyToolCardDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
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
}
