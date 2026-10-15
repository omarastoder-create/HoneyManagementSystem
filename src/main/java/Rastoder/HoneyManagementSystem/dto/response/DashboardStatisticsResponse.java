package Rastoder.HoneyManagementSystem.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record DashboardStatisticsResponse(
        BigDecimal totalRevenue,
        BigDecimal totalExpenses,
        BigDecimal netProfit,
        PerformerStat topCustomerOfMonth,
        PerformerStat topCustomerOfYear,
        PerformerStat topSellerOfMonth,
        PerformerStat topSellerOfYear,
        List<MonthlyRevenueStat> monthlySales
) {
}
