package pl.biketrack.bike.dto.response;

import com.neovisionaries.i18n.CurrencyCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BikeRepairStatisticsResponse(
        long totalRepairs,
        BigDecimal totalRepairCost,
        CurrencyCode repairsCurrency,
        LocalDateTime dateOfLastRepair,
        LocalDateTime dateOfFirstRepair,
        BigDecimal averageRepairCost,
        long repairsThisYear
) {}