package pl.biketrack.repair.dto;

import com.neovisionaries.i18n.CurrencyCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.nonNull;

public record RepairStatisticsDto(BigDecimal cost,
                                  CurrencyCode currency,
                                  LocalDateTime createdDate) {

    public static long getTotalRepairs(List<RepairStatisticsDto> repairStatisticsDto) {
        return repairStatisticsDto.size();
    }

    public static BigDecimal getTotalRepairCost(List<RepairStatisticsDto> repairStatisticsDto) {
        return repairStatisticsDto.stream()
                                  .map(RepairStatisticsDto::cost)
                                  .filter(Objects::nonNull)
                                  .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal getAverageRepairCost(long totalRepairs, BigDecimal totalRepairCost) {
        return totalRepairs > 0 ? totalRepairCost.divide(BigDecimal.valueOf(totalRepairs), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public static LocalDateTime getLastRepairDate(List<RepairStatisticsDto> repairStatisticsDto) {
        return repairStatisticsDto.stream()
                                  .map(RepairStatisticsDto::createdDate)
                                  .filter(Objects::nonNull)
                                  .max(Comparator.naturalOrder())
                                  .orElse(null);
    }

    public static LocalDateTime getFirstRepairDate(List<RepairStatisticsDto> repairStatisticsDto) {
        return repairStatisticsDto.stream()
                                  .map(RepairStatisticsDto::createdDate)
                                  .filter(Objects::nonNull)
                                  .min(Comparator.naturalOrder())
                                  .orElse(null);
    }

    public static CurrencyCode getRepairsCurrency(List<RepairStatisticsDto> repairStatisticsDto) {
        return repairStatisticsDto.stream()
                                  .map(RepairStatisticsDto::currency)
                                  .filter(Objects::nonNull)
                                  .findFirst()
                                  .orElse(null);
    }

    public static long getRepairsInYear(List<RepairStatisticsDto> repairStatisticsDto, int year) {
        return repairStatisticsDto.stream()
                                  .filter(repair -> nonNull(repair.createdDate) && repair.createdDate.getYear() == year)
                                  .toList()
                                  .size();
    }
}