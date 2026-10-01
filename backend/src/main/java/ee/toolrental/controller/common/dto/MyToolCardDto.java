package ee.toolrental.controller.common.dto;

import lombok.Data;

@Data
public class MyToolCardDto {
    private Integer toolId;
    private String toolName;
    private String toolStatus;
    private String imageData;
}
