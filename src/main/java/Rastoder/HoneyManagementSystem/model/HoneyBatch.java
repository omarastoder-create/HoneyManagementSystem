package Rastoder.HoneyManagementSystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "honey_batches")
@SQLDelete(sql = "UPDATE honey_batches SET is_deleted = true WHERE batch_id = ?")
@SQLRestriction("is_deleted = false")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class HoneyBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "batch_id", nullable = false, updatable = false)
    private UUID batchId;

    @Column(updatable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType type;

    @Column(name = "initial_weight_in_kg", nullable = false, updatable = false)
    private BigDecimal initialWeightInKg;

    @Column(name = "remaining_weight_in_kg", nullable = false)
    private BigDecimal remainingWeightInKg;

    @Column(name = "price_per_unit", nullable = false)
    private BigDecimal pricePerUnit;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @OneToMany(mappedBy = "honeyBatch", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Builder.Default
    private Set<SaleTransaction> transactions = new HashSet<>();
}