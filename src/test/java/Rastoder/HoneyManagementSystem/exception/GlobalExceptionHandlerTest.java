
package Rastoder.HoneyManagementSystem.exception;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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
        var fieldError = new org.springframework.validation.FieldError(
                "customerDto",
                "name",
                "Name must not be blank"
        );

        var bindingResult =
                new org.springframework.validation.BeanPropertyBindingResult(
                        new Object(), "customerDto");

        bindingResult.addError(fieldError);

        var exception = new MethodArgumentNotValidException(
                null, bindingResult);

        // When
        var response = handler.handleValidationExceptions(exception);

        // Then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .containsEntry("name", "Name must not be blank");
    }

    @Test
    @DisplayName("Validation exception: should return multiple field errors")
    void handleValidationExceptions_ShouldReturnMultipleErrors() {
        // Given
        var bindingResult =
                new org.springframework.validation.BeanPropertyBindingResult(
                        new Object(), "customerDto");

        bindingResult.addError(new org.springframework.validation.FieldError(
                "customerDto", "name", "Name is required"));

        bindingResult.addError(new org.springframework.validation.FieldError(
                "customerDto", "email", "Email is invalid"));

        var exception = new MethodArgumentNotValidException(
                null, bindingResult);

        // When
        var response = handler.handleValidationExceptions(exception);

        // Then
        assertThat(response.getBody())
                .containsEntry("name", "Name is required")
                .containsEntry("email", "Email is invalid");
    }

    @Test
    @DisplayName("Validation exception: should return 400 even with no field errors")
    void handleValidationExceptions_WithNoFieldErrors_ShouldReturnBadRequest() {
        // Given
        var bindingResult =
                new org.springframework.validation.BeanPropertyBindingResult(
                        new Object(), "customerDto");

        var exception = new MethodArgumentNotValidException(
                null, bindingResult);

        // When
        var response = handler.handleValidationExceptions(exception);

        // Then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEmpty();
    }
}
