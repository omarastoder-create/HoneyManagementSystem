package Rastoder.HoneyManagementSystem.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record TransactionRequest(
        @NotNull(message = "Seller ID is required") UUID sellerId,
        @NotNull(message = "Customer ID is required") UUID customerId,
        @NotEmpty(message = "Items list cannot be empty")
        @Valid List<TransactionItemRequest> items
) {}