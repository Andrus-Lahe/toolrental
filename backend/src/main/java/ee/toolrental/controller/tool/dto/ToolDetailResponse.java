package ee.toolrental.controller.tool.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ToolDetailResponse implements Serializable {
    private Integer toolId;
    private Integer ownerId;
    private String toolName;
    private String categoryName;
    private String description;
    private String imageData;
    private String status;
}
