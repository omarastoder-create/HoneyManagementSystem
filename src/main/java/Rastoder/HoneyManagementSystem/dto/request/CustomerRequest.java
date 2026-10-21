package Rastoder.HoneyManagementSystem.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CustomerRequest(
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Family name is required") String familyName,
        String description,
        // International format, e.g. +352621123456
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+?[1-9]\\d{5,14}$", message = "Invalid phone number format")
        String phoneNumber,
        @Email(message = "Invalid email format") String email
) {
}
