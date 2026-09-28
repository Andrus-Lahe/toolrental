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
public class CategoryDetailedInfoDto implements Serializable {
    private Integer id;
    private String categoryName;
    private String description;
}