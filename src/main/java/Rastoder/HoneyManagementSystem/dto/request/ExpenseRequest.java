// src/main/java/Rastoder/HoneyManagementSystem/dto/request/ExpenseRequestDto.java
package Rastoder.HoneyManagementSystem.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(
        @NotBlank(message = "Titel ist erforderlich")
        String title,
        String supplierName,
        @NotNull(message = "Datum ist erforderlich")
        LocalDate expenseDate,
        @NotNull(message = "Betrag ist erforderlich")
        @DecimalMin(value = "0.01", message = "Betrag muss größer als 0 sein")
        BigDecimal amount,
        String receiptImagePath
) {}