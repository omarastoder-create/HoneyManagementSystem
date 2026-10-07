package Rastoder.HoneyManagementSystem.dto.response;

import java.util.UUID;

public record SellerResponse(
        UUID sellerId,
        String name
) {
}
