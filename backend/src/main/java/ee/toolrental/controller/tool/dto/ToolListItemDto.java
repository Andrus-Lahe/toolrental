package ee.toolrental.controller.tool.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link ee.toolrental.persistence.tool.ToolListRow}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ToolListItemDto implements Serializable {
    private Integer toolId;
    private String toolName;
    private String description;
    private String imageData;
    private String status;
    private String cityName;
    private String districtName;
}
