package Rastoder.HoneyManagementSystem.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CustomerRequest(
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Family name is required") String familyName,
        String description,
        @NotBlank(message = "Phone number is required") String phoneNumber
) {
}
