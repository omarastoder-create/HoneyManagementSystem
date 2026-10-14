package Rastoder.HoneyManagementSystem.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionItemRequest(
        @NotNull(message = "Product ID is required") UUID productId,
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1") Integer quantity,
        @PositiveOrZero(message = "Sonderpreis darf nicht negatief sein")
        BigDecimal customPricePerUnit,
        boolean isGift
) {}