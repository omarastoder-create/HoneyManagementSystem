package Rastoder.HoneyManagementSystem.repository;

import Rastoder.HoneyManagementSystem.model.SaleTransaction;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SaleTransactionRepository extends JpaRepository<SaleTransaction, UUID> {

    @EntityGraph(attributePaths = {"customer", "seller", "items", "items.product"})
    Optional<SaleTransaction> findWithDetailsByTransactionId(UUID transactionId);
}