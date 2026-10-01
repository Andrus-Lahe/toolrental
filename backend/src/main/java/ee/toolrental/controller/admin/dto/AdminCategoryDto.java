package ee.toolrental.controller.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link ee.toolrental.persistence.category.Category}
 * Admini kategooriate tabeli rida: erinevalt avalikust CategoryDto-st sisaldab kirjeldust ja järjekorranumbrit.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminCategoryDto implements Serializable {
    private Integer categoryId;
    private String categoryName;
    private String description;
    private Integer sequence;
}
