package Rastoder.HoneyManagementSystem.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductRequest(
        @NotBlank String description,
        @NotNull @Positive BigDecimal pricePerUnit,
        @Min(1) int unitsToSell,
        @NotNull LocalDateTime dateOfHarvest
) {}