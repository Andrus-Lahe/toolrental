package ee.toolrental.controller.appuser.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * DTO for {@link ee.toolrental.persistence.appuser.AppUser}
 */
@Data
public class CurrentUserDto implements Serializable {
    Integer userId;
    String roleName;

    String firstName;

    String lastName;

    String email;

    boolean hasProfile;


}