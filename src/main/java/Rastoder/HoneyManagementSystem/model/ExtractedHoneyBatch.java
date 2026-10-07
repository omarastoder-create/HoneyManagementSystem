package Rastoder.HoneyManagementSystem.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("EXTRACTED_HONEY")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
public class ExtractedHoneyBatch extends Product {

    @Enumerated(EnumType.STRING)
    @Column(name = "honey_state", nullable = false)
    private HoneyState state;

    @Column(name = "initial_weight_in_kg", updatable = false, nullable = false)
    private BigDecimal initialWeightInKg;

    @Column(name = "remaining_weight_in_kg", nullable = false)
    private BigDecimal remainingWeightInKg;
}