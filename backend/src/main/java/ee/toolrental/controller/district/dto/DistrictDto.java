package ee.toolrental.controller.district.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link ee.toolrental.persistence.district.District}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DistrictDto implements Serializable {
    private Integer districtId;
    private String districtName;
}
