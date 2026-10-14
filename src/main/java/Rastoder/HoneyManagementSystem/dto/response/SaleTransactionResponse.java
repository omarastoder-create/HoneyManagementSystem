package Rastoder.HoneyManagementSystem.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SaleTransactionResponse(
        UUID transactionId,
        LocalDateTime saleDate,
        String sellerName,
        String customerName,
        BigDecimal totalPrice,
        List<TransactionItemResponse> items
) {
    public SaleTransactionResponse {
        items = items != null ? List.copyOf(items) : List.of();
    }
}