package Rastoder.HoneyManagementSystem.service;

import Rastoder.HoneyManagementSystem.dto.response.DashboardStatisticsResponse;
import Rastoder.HoneyManagementSystem.dto.response.MonthlyRevenueStat;
import Rastoder.HoneyManagementSystem.dto.response.PerformerStat;
import Rastoder.HoneyManagementSystem.repository.ExpenseRepository;
import Rastoder.HoneyManagementSystem.repository.SaleTransactionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class StatisticsService {

    private static final Pageable TOP_ONE = PageRequest.of(0, 1);

    private final SaleTransactionRepository saleTransactionRepository;
    private final ExpenseRepository expenseRepository;

    public StatisticsService(SaleTransactionRepository saleTransactionRepository, ExpenseRepository expenseRepository) {
        this.saleTransactionRepository = saleTransactionRepository;
        this.expenseRepository = expenseRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatisticsResponse getDashboardStatistics() {
        BigDecimal revenue = saleTransactionRepository.getTotalRevenue();
        if (revenue == null) revenue = BigDecimal.ZERO;

        BigDecimal expenses = expenseRepository.getTotalExpenses();
        if (expenses == null) expenses = BigDecimal.ZERO;

        BigDecimal netProfit = revenue.subtract(expenses);

        LocalDate now = LocalDate.now();
        LocalDateTime startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
        LocalDateTime endOfMonth = now.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59);
        LocalDateTime startOfYear = now.with(TemporalAdjusters.firstDayOfYear()).atStartOfDay();
        LocalDateTime endOfYear = now.with(TemporalAdjusters.lastDayOfYear()).atTime(23, 59, 59);

        PerformerStat topCustomerMonth = saleTransactionRepository
                .findTopCustomersInPeriod(startOfMonth, endOfMonth, TOP_ONE)
                .stream()
                .findFirst()
                .orElseGet(PerformerStat::empty);

        PerformerStat topCustomerYear = saleTransactionRepository
                .findTopCustomersInPeriod(startOfYear, endOfYear, TOP_ONE)
                .stream()
                .findFirst()
                .orElseGet(PerformerStat::empty);

        PerformerStat topSellerMonth = saleTransactionRepository
                .findTopSellersInPeriod(startOfMonth, endOfMonth, TOP_ONE)
                .stream()
                .findFirst()
                .orElseGet(PerformerStat::empty);

        PerformerStat topSellerYear = saleTransactionRepository
                .findTopSellersInPeriod(startOfYear, endOfYear, TOP_ONE)
                .stream()
                .findFirst()
                .orElseGet(PerformerStat::empty);

        List<MonthlyRevenueStat> monthlySales = saleTransactionRepository.findMonthlySalesByYear(now.getYear());

        return new DashboardStatisticsResponse(
                revenue,
                expenses,
                netProfit,
                topCustomerMonth,
                topCustomerYear,
                topSellerMonth,
                topSellerYear,
                monthlySales
        );
    }
}