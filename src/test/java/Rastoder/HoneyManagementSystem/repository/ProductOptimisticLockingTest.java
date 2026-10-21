package Rastoder.HoneyManagementSystem.repository;

import Rastoder.HoneyManagementSystem.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Runs against the real (H2) database, because optimistic locking is done by
 * Hibernate's "UPDATE ... WHERE version = ?" and cannot be tested with mocks.
 */
@SpringBootTest
class ProductOptimisticLockingTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("Saving a stale copy of a product should fail instead of overwriting the newer stock")
    void saveStaleProduct_ShouldThrowOptimisticLockingFailure() {
        // Given - a product with 5 jars
        Product saved = productRepository.save(Product.builder()
                .description("Akazienhonig")
                .pricePerUnit(new BigDecimal("10.00"))
                .unitsToSell(5)
                .dateOfHarvest(LocalDate.now())
                .build());

        // Two "requests" load the product at the same time (both see version 0, 5 jars)
        Product requestA = productRepository.findById(saved.getProductId()).orElseThrow();
        Product requestB = productRepository.findById(saved.getProductId()).orElseThrow();

        // Request A sells 4 jars and commits first -> version becomes 1
        requestA.reduceStock(4);
        productRepository.save(requestA);

        // Request B still thinks there are 5 jars and also sells 4
        requestB.reduceStock(4);

        // When / Then - B's update is rejected instead of overselling
        assertThatThrownBy(() -> productRepository.save(requestB))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);

        Product current = productRepository.findById(saved.getProductId()).orElseThrow();
        assertThat(current.getUnitsToSell()).isEqualTo(1);
        assertThat(current.getVersion()).isEqualTo(1L);
    }
}
