package ee.toolrental.controller.admin.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Request DTO kasutaja oleku muutmiseks: B = blokeeri, A = aktiivne.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStatusRequestDto implements Serializable {
    @NotNull(message = "ei tohi olla tühi")
    @Pattern(regexp = "[AB]", message = "peab olema A või B")
    private String status;
}
