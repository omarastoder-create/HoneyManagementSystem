package Rastoder.HoneyManagementSystem.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ExpenseResponse(
        UUID expenseId,
        String title,
        String supplierName,
        LocalDate expenseDate,
        BigDecimal amount,
        String receiptUrl
) {}
