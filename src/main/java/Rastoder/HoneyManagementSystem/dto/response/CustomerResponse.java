package Rastoder.HoneyManagementSystem.dto.response;

import java.util.UUID;

public record CustomerResponse(
        UUID customerId,
        String name,
        String familyName,
        String description,
        String phoneNumber
) {
}
