package ee.toolrental.controller.tool.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ToolCreateRequestDto {
    @NotNull(message = "on kohustuslik")
    @Positive(message = "peab olema positiivne")
    private Integer categoryId;

    @NotBlank(message = "ei tohi olla tühi")
    @Size(max = 150, message = "ei tohi ületada 150 märki")
    private String name;

    @Size(max = 2000, message = "ei tohi ületada 2000 märki")
    private String description;

    @NotNull(message = "on kohustuslik")
    private String imageData;
}
