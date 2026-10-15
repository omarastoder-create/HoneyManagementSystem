package Rastoder.HoneyManagementSystem.dto.response;

import java.math.BigDecimal;

public record PerformerStat(
        String name,
        BigDecimal totalVolume
) {
    public static PerformerStat empty() {
        return new PerformerStat("Keine Daten", BigDecimal.ZERO);
    }
}