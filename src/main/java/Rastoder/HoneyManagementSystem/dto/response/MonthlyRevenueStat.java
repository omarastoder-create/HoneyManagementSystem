package Rastoder.HoneyManagementSystem.dto.response;

import java.math.BigDecimal;

public record MonthlyRevenueStat(
        int year,
        int month,
        BigDecimal revenue
) {
}
