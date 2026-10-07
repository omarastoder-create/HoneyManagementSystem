package Rastoder.HoneyManagementSystem.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SellerRequest(
        @NotBlank(message = "Name is required") String name
) {
}
