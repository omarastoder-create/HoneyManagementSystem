package Rastoder.HoneyManagementSystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "transaction_items")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class TransactionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "item_id", nullable = false, updatable = false)
    private UUID itemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false, updatable = false)
    private SaleTransaction transaction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    private Product product;

    @Column(nullable = false, updatable = false)
    private Integer quantity;

    // Snapshot of the price actually charged per unit at sale time (0 for gifts).
    @Column(name = "unit_price", nullable = false, updatable = false)
    private BigDecimal unitPrice;

    @Column(name = "is_gift", nullable = false, updatable = false)
    private boolean gift;

    @Column(name = "subtotal_price", nullable = false, updatable = false)
    private BigDecimal subtotalPrice;
}