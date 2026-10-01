package ee.toolrental.infrastructure.exception;

import lombok.Getter;

@Getter
public class IncorrectInputException extends RuntimeException {
    private final String message;
    private final String errorCode;

    public IncorrectInputException(String message) {
        super(message);
        this.message = message;
        this.errorCode = "INCORRECT_INPUT";
    }
}
