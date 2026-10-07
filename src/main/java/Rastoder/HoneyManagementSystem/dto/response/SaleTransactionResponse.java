package Rastoder.HoneyManagementSystem.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SaleTransactionResponse(
        UUID transactionId,
        String sellerName,
        String customerName,
        String batchDescription,
        BigDecimal quantity,
        BigDecimal totalPrice,
        LocalDateTime saleDate
) {}