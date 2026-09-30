package ee.toolrental.controller.category.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link ee.toolrental.persistence.category.Category}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto implements Serializable {
    private Integer categoryId;
    private String categoryName;
}
