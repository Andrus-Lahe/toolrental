package ee.toolrental.controller.profile.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * DTO for {@link ee.toolrental.persistence.profile.Profile}
 */
@Data
public class ProfileDto implements Serializable {
    private Integer userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Integer cityId;
    private Integer districtId;
    private String streetName;
    private String houseNumber;
    private String apartmentNumber;
    private Boolean hasProfile;
}
