package ee.toolrental.controller.profile.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * DTO for {@link ee.toolrental.persistence.profile.Profile}
 */
@Data
public class ProfileUpdateRequestDto implements Serializable {
    @NotBlank(message = "ei tohi olla tühi")
    @Size(max = 100, message = "võib olla kuni 100 märki")
    private String firstName;

    @NotBlank(message = "ei tohi olla tühi")
    @Size(max = 100, message = "võib olla kuni 100 märki")
    private String lastName;

    @NotBlank(message = "ei tohi olla tühi")
    @Email(message = "peab olema korrektne e-posti aadress")
    @Size(max = 254, message = "võib olla kuni 254 märki")
    private String email;

    @NotBlank(message = "ei tohi olla tühi")
    @Size(max = 32, message = "võib olla kuni 32 märki")
    private String phone;

    @NotNull(message = "ei tohi olla tühi")
    private Integer districtId;

    @NotBlank(message = "ei tohi olla tühi")
    @Size(max = 150, message = "võib olla kuni 150 märki")
    private String streetName;

    @NotBlank(message = "ei tohi olla tühi")
    @Size(max = 20, message = "võib olla kuni 20 märki")
    private String houseNumber;

    @Size(max = 20, message = "võib olla kuni 20 märki")
    private String apartmentNumber;
}
