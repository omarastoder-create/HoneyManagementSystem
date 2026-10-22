package Rastoder.HoneyManagementSystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "products")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Column(updatable = false)
    private String description;

    @Column(name = "price_per_unit", nullable = false)
    private BigDecimal pricePerUnit;

    @Column(name = "units_harvested")
    private int unitsToSell;

    @Column(name = "date_of_harvest", nullable = false)
    private LocalDate dateOfHarvest;

    @Column(name = "image_url")
    private String imageUrl;

    // Optimistic locking: Hibernate adds "WHERE version = ?" to every UPDATE.
    // If another transaction changed this product in the meantime, the update
    // matches 0 rows and fails instead of silently overwriting the stock.
    @Version
    private Long version;

    public void reduceStock(int quantity) {
        if (quantity > this.unitsToSell) {
            throw new IllegalArgumentException("Nicht genug Bestand für Produkt: " + this.description);
        }
        this.unitsToSell -= quantity;
    }
}