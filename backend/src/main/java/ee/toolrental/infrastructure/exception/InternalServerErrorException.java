package ee.toolrental.infrastructure.exception;

import lombok.Getter;

@Getter
public class InternalServerErrorException extends RuntimeException {
    private final String message;
    private final String errorCode;

    public InternalServerErrorException(String message) {
        super(message);
        this.message = message;
        this.errorCode = "INTERNAL_SERVER_ERROR";
    }
}
