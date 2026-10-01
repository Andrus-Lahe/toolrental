package ee.toolrental.controller.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Request DTO kategooria muutmiseks (PUT asendab kogu sisu). Kirjeldus on valikuline (võib olla null),
 * nimi ja järjekorranumber on kohustuslikud.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryUpdateRequestDto implements Serializable {
    @NotBlank(message = "ei tohi olla tühi")
    @Size(max = 100, message = "ei tohi olla pikem kui 100 märki")
    private String categoryName;

    @Size(max = 255, message = "ei tohi olla pikem kui 255 märki")
    private String description;

    @NotNull(message = "ei tohi olla tühi")
    private Integer sequence;
}
