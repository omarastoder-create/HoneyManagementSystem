package Rastoder.HoneyManagementSystem.service;

import Rastoder.HoneyManagementSystem.dto.response.DashboardStatisticsResponse;
import Rastoder.HoneyManagementSystem.dto.response.MonthlyRevenueStat;
import Rastoder.HoneyManagementSystem.dto.response.PerformerStat;
import Rastoder.HoneyManagementSystem.repository.ExpenseRepository;
import Rastoder.HoneyManagementSystem.repository.SaleTransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private SaleTransactionRepository saleTransactionRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    @Test
    @DisplayName("getDashboardStatistics: When data exists, should calculate totals and return dashboard stats")
    void getDashboardStatistics_WithData_ShouldReturnStats() {
        // Given
        int currentYear = LocalDate.now().getYear();
        when(saleTransactionRepository.getTotalRevenue()).thenReturn(new BigDecimal("1000.00"));
        when(expenseRepository.getTotalExpenses()).thenReturn(new BigDecimal("300.00"));

        var topCustomer = new PerformerStat("Customer A", new BigDecimal("500.00"));
        var topSeller = new PerformerStat("Seller A", new BigDecimal("1000.00"));
        var monthlyStat = new MonthlyRevenueStat(currentYear, 1, new BigDecimal("1000.00"));

        when(saleTransactionRepository.findTopCustomersInPeriod(
                any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(topCustomer));
        when(saleTransactionRepository.findTopSellersInPeriod(
                any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(topSeller));
        when(saleTransactionRepository.findMonthlySalesByYear(anyInt()))
                .thenReturn(List.of(monthlyStat));

        // When
        DashboardStatisticsResponse response = statisticsService.getDashboardStatistics();

        // Then
        verify(saleTransactionRepository).getTotalRevenue();
        verify(expenseRepository).getTotalExpenses();
        verify(saleTransactionRepository).findMonthlySalesByYear(currentYear);

        assertThat(response).isEqualTo(new DashboardStatisticsResponse(
                new BigDecimal("1000.00"),
                new BigDecimal("300.00"),
                new BigDecimal("700.00"),
                topCustomer,
                topCustomer,
                topSeller,
                topSeller,
                List.of(monthlyStat)
        ));
    }

    @Test
    @DisplayName("getDashboardStatistics: When revenue and expenses are null and lists are empty, should handle defaults")
    void getDashboardStatistics_WhenNullAndEmpty_ShouldFallbackToDefaults() {
        // Given
        when(saleTransactionRepository.getTotalRevenue()).thenReturn(null);
        when(expenseRepository.getTotalExpenses()).thenReturn(null);

        when(saleTransactionRepository.findTopCustomersInPeriod(
                any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of());
        when(saleTransactionRepository.findTopSellersInPeriod(
                any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of());
        when(saleTransactionRepository.findMonthlySalesByYear(anyInt()))
                .thenReturn(List.of());

        // When
        DashboardStatisticsResponse response = statisticsService.getDashboardStatistics();

        // Then
        assertThat(response).isEqualTo(new DashboardStatisticsResponse(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                PerformerStat.empty(),
                PerformerStat.empty(),
                PerformerStat.empty(),
                PerformerStat.empty(),
                List.of()
        ));
    }
}