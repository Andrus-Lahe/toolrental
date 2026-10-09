package ee.toolrental.infrastructure.exception;

import lombok.Getter;

/**
 * Visatakse, kui AI turvakontroll (guardrail) lükkab kasutaja küsimuse tagasi.
 * guardrail ja reason on ainult logimiseks, kliendile neid ei tagastata.
 */
@Getter
public class GuardrailViolationException extends RuntimeException {
    private static final String CLIENT_MESSAGE = "Küsimus lükati turvakontrolli tõttu tagasi. Palun sõnasta küsimus ümber.";

    private final String message;
    private final String errorCode;
    private final String guardrail;
    private final String reason;

    public GuardrailViolationException(String guardrail, String reason) {
        super(CLIENT_MESSAGE);
        this.message = CLIENT_MESSAGE;
        this.errorCode = "QUESTION_REJECTED";
        this.guardrail = guardrail;
        this.reason = reason;
    }
}
