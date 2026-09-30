package ee.toolrental.controller.ask.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Vastuse keha teenusele POST /api/admin/ask: AI koostatud vastus tavakeeles.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AskResponse implements Serializable {
    private String answer;
}
