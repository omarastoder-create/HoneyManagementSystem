package Rastoder.HoneyManagementSystem.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ProductResponse(
        UUID productId,
        String description,
        BigDecimal pricePerUnit,
        int unitsToSell,
        LocalDate dateOfHarvest,
        String imageUrl
) {
}
