package ee.toolrental.controller.appuser.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link ee.toolrental.persistence.appuser.AppUser}
 */
@Value
public class AppUserDto implements Serializable {
    Integer id;
    Integer roleId;
    String roleRoleName;
    @NotNull
    @Size(max = 100)
    String firstName;
    @NotNull
    @Size(max = 100)
    String lastName;
    @NotNull
    @Size(max = 255)
    String googleSub;
    @NotNull
    @Size(max = 1)
    String status;
}