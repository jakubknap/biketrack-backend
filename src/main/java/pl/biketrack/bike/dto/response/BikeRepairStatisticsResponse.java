package pl.biketrack.bike.dto.response;

import pl.biketrack.dashboard.dto.MoneyDto;

import java.time.LocalDateTime;

public record BikeRepairStatisticsResponse(
        long totalRepairs,
        MoneyDto totalRepairCost,
        LocalDateTime dateOfLastRepair,
        LocalDateTime dateOfFirstRepair,
        MoneyDto averageRepairCost,
        long repairsThisYear
) {}