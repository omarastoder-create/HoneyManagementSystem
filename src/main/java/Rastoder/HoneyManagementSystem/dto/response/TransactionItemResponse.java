package Rastoder.HoneyManagementSystem.dto.response;

import java.math.BigDecimal;

public record TransactionItemResponse(
        String productDescription,
        int units,
        BigDecimal pricePerUnit,
        BigDecimal subTotal
) {}