package Rastoder.HoneyManagementSystem.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

// The receipt is not part of this JSON request: it is uploaded as a file via
// PUT /api/v1/expenses/{id}/receipt, so clients can never set an arbitrary path.
public record ExpenseRequest(
        @NotBlank(message = "Titel ist erforderlich")
        String title,
        String supplierName,
        @NotNull(message = "Datum ist erforderlich")
        LocalDate expenseDate,
        @NotNull(message = "Betrag ist erforderlich")
        @DecimalMin(value = "0.01", message = "Betrag muss größer als 0 sein")
        BigDecimal amount
) {}
