package Rastoder.HoneyManagementSystem.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionItemResponse(
        UUID itemId,
        UUID productId,
        Integer quantity,
        BigDecimal subtotalPrice
) {}