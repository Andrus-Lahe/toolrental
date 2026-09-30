package ee.toolrental.controller.ask.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * Päringu keha teenusele POST /api/admin/ask: kasutaja küsimus tavakeeles.
 */
@Data
public class AskRequest implements Serializable {
    @NotBlank(message = "ei tohi olla tühi")
    @Size(max = 500, message = "võib olla kuni 500 märki")
    private String question;
}
