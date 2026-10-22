// src/main/java/Rastoder/HoneyManagementSystem/model/Expense.java
package Rastoder.HoneyManagementSystem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "expenses")
@Getter
@Setter
@NoArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID expenseId;

    @NotBlank(message = "Titel oder Beschreibung ist erforderlich")
    private String title;

    private String supplierName;

    @NotNull(message = "Datum ist erforderlich")
    private LocalDate expenseDate;

    @NotNull(message = "Betrag ist erforderlich")
    @DecimalMin(value = "0.01", message = "Betrag muss größer als 0 sein")
    private BigDecimal amount;

    private String category;

    // Public URL of the uploaded receipt (PDF or photo), set only via the receipt upload endpoint
    @Column(name = "receipt_url")
    private String receiptUrl;
}