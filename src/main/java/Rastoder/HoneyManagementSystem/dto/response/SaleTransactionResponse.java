package Rastoder.HoneyManagementSystem.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SaleTransactionResponse(
        UUID transactionId,
        LocalDateTime saleDate,
        UUID sellerId,
        UUID customerId,
        BigDecimal totalPrice,
        List<TransactionItemResponse> items
) {}