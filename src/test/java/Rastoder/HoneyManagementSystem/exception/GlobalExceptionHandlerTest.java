package Rastoder.HoneyManagementSystem.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    @DisplayName("EntityNotFoundException: should return 404")
    void handleEntityNotFound_ShouldReturnNotFound() {
        // Given
        var exception =
                new EntityNotFoundException("Customer not found");

        // When
        var response = handler.handleEntityNotFound(exception);

        // Then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().error()).isEqualTo("Not Found");
        assertThat(response.getBody().message())
                .isEqualTo("Customer not found");
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    @DisplayName("IllegalArgumentException: should return 400")
    void handleIllegalArgument_ShouldReturnBadRequest() {
        // Given
        var exception =
                new IllegalArgumentException("Invalid input");

        // When
        var response = handler.handleIllegalArgument(exception);

        // Then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Bad Request");
        assertThat(response.getBody().message()).isEqualTo("Invalid input");
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Validation exception: should return field errors")
    void handleValidationExceptions_ShouldReturnFieldErrors() {
        // Given
        var bindingResult = new BeanPropertyBindingResult(new Object(), "customerDto");
        bindingResult.addError(new FieldError("customerDto", "name", "Name must not be blank"));
        var exception = new MethodArgumentNotValidException(null, bindingResult);

        // When
        var response = handler.handleValidationExceptions(exception);

        // Then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Validation failed");
        assertThat(response.getBody().fieldErrors())
                .containsEntry("name", "Name must not be blank");
    }

    @Test
    @DisplayName("Validation exception: should return multiple field errors")
    void handleValidationExceptions_ShouldReturnMultipleErrors() {
        // Given
        var bindingResult = new BeanPropertyBindingResult(new Object(), "customerDto");
        bindingResult.addError(new FieldError("customerDto", "name", "Name is required"));
        bindingResult.addError(new FieldError("customerDto", "email", "Email is invalid"));
        var exception = new MethodArgumentNotValidException(null, bindingResult);

        // When
        var response = handler.handleValidationExceptions(exception);

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().fieldErrors())
                .containsEntry("name", "Name is required")
                .containsEntry("email", "Email is invalid");
    }

    @Test
    @DisplayName("Validation exception: should return 400 even with no field errors")
    void handleValidationExceptions_WithNoFieldErrors_ShouldReturnBadRequest() {
        // Given
        var bindingResult = new BeanPropertyBindingResult(new Object(), "customerDto");
        var exception = new MethodArgumentNotValidException(null, bindingResult);

        // When
        var response = handler.handleValidationExceptions(exception);

        // Then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().fieldErrors()).isEmpty();
    }

    record Sample(@NotBlank(message = "Title is required") String title) {}

    @Test
    @DisplayName("ConstraintViolationException: should return 400 with field errors")
    void handleConstraintViolation_ShouldReturnFieldErrors() {
        // Given
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            var exception = new ConstraintViolationException(validator.validate(new Sample("")));

            // When
            var response = handler.handleConstraintViolation(exception);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().fieldErrors()).containsEntry("title", "Title is required");
        }
    }

    @Test
    @DisplayName("DataIntegrityViolationException: should return 409 without leaking SQL details")
    void handleDataIntegrity_ShouldReturnConflict() {
        // Given
        var exception = new DataIntegrityViolationException("could not execute statement",
                new SQLException("Unique index violation: CUSTOMERS(PHONE_NUMBER)"));

        // When
        var response = handler.handleDataIntegrity(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message())
                .isEqualTo("The data conflicts with an existing record")
                .doesNotContain("PHONE_NUMBER");
    }

    @Test
    @DisplayName("MaxUploadSizeExceededException: should return 413")
    void handleMaxUploadSize_ShouldReturnContentTooLarge() {
        // When
        var response = handler.handleMaxUploadSize(new MaxUploadSizeExceededException(8_000_000));

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONTENT_TOO_LARGE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(413);
    }

    @Test
    @DisplayName("Fallback: Spring web exceptions keep their own status (e.g. 404 for unknown URL)")
    void handleUnexpected_WithSpringWebException_ShouldKeepItsStatus() {
        // Given
        var exception = new NoResourceFoundException(HttpMethod.GET, "/api/v1/unknown", "api/v1/unknown");

        // When
        var response = handler.handleUnexpected(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
    }

    @Test
    @DisplayName("Fallback: unknown exceptions return 500 with a generic message")
    void handleUnexpected_WithUnknownException_ShouldReturnGenericServerError() {
        // When
        var response = handler.handleUnexpected(new IllegalStateException("secret internal detail"));

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message())
                .isEqualTo("An unexpected error occurred")
                .doesNotContain("secret");
    }
}
