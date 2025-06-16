package pl.biketrack.user.dto.response;

import com.neovisionaries.i18n.CurrencyCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserStatisticsResponse(
        long totalBikes,
        long totalRepairs,
        BigDecimal totalRepairCost,
        BigDecimal averageRepairCost,
        CurrencyCode repairsCurrency,
        LocalDateTime lastRepairDate
) {}