package Rastoder.HoneyManagementSystem.repository;

import Rastoder.HoneyManagementSystem.dto.response.MonthlyRevenueStat;
import Rastoder.HoneyManagementSystem.dto.response.PerformerStat;
import Rastoder.HoneyManagementSystem.model.SaleTransaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SaleTransactionRepository extends JpaRepository<SaleTransaction, UUID> {

    @EntityGraph(attributePaths = {"customer", "seller", "items", "items.product"})
    Optional<SaleTransaction> findWithDetailsByTransactionId(UUID transactionId);

    @EntityGraph(attributePaths = {"customer", "seller", "items", "items.product"})
    List<SaleTransaction> findAllByOrderBySaleDateDesc();

    @Query("SELECT COALESCE(SUM(st.totalPrice), 0) FROM SaleTransaction st")
    BigDecimal getTotalRevenue();

    @Query("""
            SELECT new Rastoder.HoneyManagementSystem.dto.response.PerformerStat(
                st.customer.name, COALESCE(SUM(st.totalPrice), 0)
            )
            FROM SaleTransaction st
            WHERE st.saleDate BETWEEN :startDateTime AND :endDateTime
            GROUP BY st.customer.customerId, st.customer.name
            ORDER BY SUM(st.totalPrice) DESC
            """)
    List<PerformerStat> findTopCustomersInPeriod(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            Pageable pageable
    );

    @Query("""
            SELECT new Rastoder.HoneyManagementSystem.dto.response.PerformerStat(
                st.seller.name, COALESCE(SUM(st.totalPrice), 0)
            )
            FROM SaleTransaction st
            WHERE st.saleDate BETWEEN :startDateTime AND :endDateTime
            GROUP BY st.seller.sellerId, st.seller.name
            ORDER BY SUM(st.totalPrice) DESC
            """)
    List<PerformerStat> findTopSellersInPeriod(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            Pageable pageable

    );

    @Query("""
            SELECT new Rastoder.HoneyManagementSystem.dto.response.MonthlyRevenueStat(
                YEAR(st.saleDate), MONTH(st.saleDate), COALESCE(SUM(st.totalPrice), 0)
            )
            FROM SaleTransaction st
            WHERE YEAR(st.saleDate) = :year
            GROUP BY YEAR(st.saleDate), MONTH(st.saleDate)
            ORDER BY MONTH(st.saleDate) ASC
            """)
    List<MonthlyRevenueStat> findMonthlySalesByYear(@Param("year") int year);
}