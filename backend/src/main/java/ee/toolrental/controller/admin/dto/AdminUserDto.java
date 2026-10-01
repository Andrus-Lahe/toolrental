package ee.toolrental.controller.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link ee.toolrental.persistence.appuser.AppUser}
 * Admini kasutajate tabeli rida: email ja registeredAt on null, kui kasutajal pole profiili.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserDto implements Serializable {
    private Integer userId;
    private String firstName;
    private String lastName;
    private String email;
    private String roleName;
    private LocalDate registeredAt;
    private String status;
}
