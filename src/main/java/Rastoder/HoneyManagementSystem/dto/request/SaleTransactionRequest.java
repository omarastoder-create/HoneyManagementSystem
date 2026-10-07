package Rastoder.HoneyManagementSystem.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record SaleTransactionRequest(
        @NotNull(message = "Seller ID is required")
        UUID sellerId,

        @NotNull(message = "Customer ID is required")
        UUID customerId,

        @NotNull(message = "Batch ID is required")
        UUID batchId,

        @NotNull
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity
) {}