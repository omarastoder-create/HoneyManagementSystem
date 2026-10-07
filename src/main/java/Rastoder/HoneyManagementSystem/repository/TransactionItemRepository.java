package Rastoder.HoneyManagementSystem.repository;

import Rastoder.HoneyManagementSystem.dto.response.ProductBestCustomer;
import Rastoder.HoneyManagementSystem.model.TransactionItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TransactionItemRepository extends JpaRepository<TransactionItem, UUID> {

    @Query("""
        SELECT new Rastoder.HoneyManagementSystem.dto.response.ProductBestCustomer(
            p.description, c.name, c.familyName, SUM(i.quantity)
        )
        FROM TransactionItem i
        JOIN i.transaction t
        JOIN t.customer c
        JOIN i.product p
        WHERE p.productId = :productId
        GROUP BY p.description, c.name, c.familyName
        ORDER BY SUM(i.quantity) DESC
    """)
    List<ProductBestCustomer> findBestCustomerStatsForProduct(@Param("productId") UUID productId, Pageable pageable);
}