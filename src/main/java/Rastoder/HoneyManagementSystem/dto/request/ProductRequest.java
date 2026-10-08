package Rastoder.HoneyManagementSystem.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductRequest(
        String description,
        BigDecimal pricePerUnit,
        int unitsToSell,
        LocalDateTime dateOfHarvest
) {
}
