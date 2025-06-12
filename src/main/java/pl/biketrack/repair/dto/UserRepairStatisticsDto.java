package pl.biketrack.repair.dto;

import com.neovisionaries.i18n.CurrencyCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public record UserRepairStatisticsDto(BigDecimal cost,
                                      CurrencyCode currency,
                                      LocalDateTime createdDate) {

    public static long getTotalRepairs(List<UserRepairStatisticsDto> userRepairStatisticsDto) {
        return userRepairStatisticsDto.size();
    }

    public static BigDecimal getTotalRepairCost(List<UserRepairStatisticsDto> userRepairStatisticsDto) {
        return userRepairStatisticsDto.stream()
                                      .map(UserRepairStatisticsDto::cost)
                                      .filter(Objects::nonNull)
                                      .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal getAverageRepairCost(long totalRepairs, BigDecimal totalRepairCost) {
        return totalRepairs > 0 ? totalRepairCost.divide(BigDecimal.valueOf(totalRepairs), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public static LocalDateTime getLastRepairDate(List<UserRepairStatisticsDto> userRepairStatisticsDto) {
        return userRepairStatisticsDto.stream()
                                      .map(UserRepairStatisticsDto::createdDate)
                                      .max(Comparator.naturalOrder())
                                      .orElse(null);
    }

    public static CurrencyCode getRepairsCurrency(List<UserRepairStatisticsDto> userRepairStatisticsDto) {
        return userRepairStatisticsDto.stream()
                                      .map(UserRepairStatisticsDto::currency)
                                      .filter(Objects::nonNull)
                                      .findFirst()
                                      .orElse(null);
    }
}