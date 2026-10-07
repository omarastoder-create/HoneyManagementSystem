package Rastoder.HoneyManagementSystem.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("HONEYCOMB")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
public class HoneycombBatch extends Product {

    @Column(name = "initial_piece_count", updatable = false, nullable = false)
    private Integer initialPieceCount;

    @Column(name = "remaining_piece_count", nullable = false)
    private Integer remainingPieceCount;
}