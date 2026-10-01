package ee.toolrental.infrastructure;

import ee.toolrental.infrastructure.error.ApiError;
import ee.toolrental.infrastructure.exception.DataNotFoundException;
import ee.toolrental.infrastructure.exception.ForbiddenException;
import ee.toolrental.infrastructure.exception.IncorrectInputException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.toolrental.infrastructure.exception.CategoryLoadingException;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ApiError> handleForbiddenException(ForbiddenException exception) {
        ApiError apiError = new ApiError();
        apiError.setMessage(exception.getMessage());
        apiError.setErrorCode(exception.getErrorCode());
        return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handleDataNotFoundException(DataNotFoundException exception) {
        ApiError apiError = new ApiError();
        apiError.setMessage(exception.getMessage());
        apiError.setErrorCode(exception.getErrorCode());
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handlePrimaryKeyNotFoundException(PrimaryKeyNotFoundException exception) {
        ApiError apiError = new ApiError();
        apiError.setMessage(exception.getMessage());
        apiError.setErrorCode(exception.getErrorCode());
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handleInternalServerErrorException(InternalServerErrorException exception) {
        ApiError apiError = new ApiError();
        apiError.setMessage(exception.getMessage());
        apiError.setErrorCode(exception.getErrorCode());
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handleIncorrectInputException(IncorrectInputException exception) {
        ApiError apiError = new ApiError();
        apiError.setMessage(exception.getMessage());
        apiError.setErrorCode("INCORRECT_INPUT");
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            org.springframework.http.@NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            org.springframework.web.context.request.@NonNull WebRequest request) {
        String field = "body";
        for (Throwable cause = ex.getCause(); cause != null; cause = cause.getCause()) {
            if (cause instanceof JacksonException jacksonException && !jacksonException.getPath().isEmpty()) {
                String propertyName = jacksonException.getPath().getFirst().getPropertyName();
                if (propertyName != null) field = propertyName;
                break;
            }
        }
        ApiError apiError = new ApiError();
        apiError.setMessage(field + ": vigane väärtus või JSON vorming");
        apiError.setErrorCode("INCORRECT_INPUT");
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            org.springframework.http.@NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            org.springframework.web.context.request.@NonNull WebRequest request) {

        FieldError firstError = ex.getBindingResult().getFieldErrors().getFirst();

        ApiError apiError = new ApiError();
        apiError.setMessage(firstError.getField() + ": " + firstError.getDefaultMessage());
        apiError.setErrorCode("INCORRECT_INPUT");

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex,
            org.springframework.http.@NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            org.springframework.web.context.request.@NonNull WebRequest request) {

        String parameterName = ex instanceof MethodArgumentTypeMismatchException methodArgumentTypeMismatchException
                ? methodArgumentTypeMismatchException.getName()
                : ex.getPropertyName();
        String requiredTypeName = ex.getRequiredType() == null ? "õiget" : ex.getRequiredType().getSimpleName();

        ApiError apiError = new ApiError();
        apiError.setMessage(parameterName + ": peab olema " + requiredTypeName + "-tüüpi täisarv");
        apiError.setErrorCode("INCORRECT_INPUT");

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleException(Exception exception) {
        log.error("Unexpected request failure", exception);
        ApiError apiError = new ApiError();
        apiError.setMessage("Toiming ebaõnnestus. Palun proovi hiljem uuesti.");
        apiError.setErrorCode("INTERNAL_SERVER_ERROR");

        return new ResponseEntity<>(apiError,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handleCategoryLoadingException(CategoryLoadingException exception) {
        ApiError apiError = new ApiError();
        apiError.setMessage(exception.getMessage());
        apiError.setErrorCode(exception.getErrorCode());

        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
