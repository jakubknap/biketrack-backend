package pl.biketrack.statistics.service;

import com.neovisionaries.i18n.CurrencyCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.biketrack.bike.repository.BikeRepository;
import pl.biketrack.dashboard.dto.MoneyDto;
import pl.biketrack.repair.dto.RepairStatisticsDto;
import pl.biketrack.repair.repository.RepairRepository;
import pl.biketrack.security.util.SecurityUtils;
import pl.biketrack.statistics.dto.RepairsPerMonthDtoProjection;
import pl.biketrack.statistics.dto.response.StatisticsResponse;
import pl.biketrack.statistics.dto.response.StatisticsResponse.AverageRepairCostPerBike;
import pl.biketrack.statistics.dto.response.StatisticsResponse.AverageRepairCostPerBikeProjection;
import pl.biketrack.statistics.dto.response.StatisticsResponse.RepairsPerBike;
import pl.biketrack.statistics.dto.response.StatisticsResponse.RepairsPerBikeProjection;
import pl.biketrack.statistics.dto.response.StatisticsResponse.RepairsThisYearPerMonth;
import pl.biketrack.statistics.dto.response.StatisticsResponse.Summary;

import java.math.BigDecimal;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.toMap;
import static pl.biketrack.repair.dto.RepairStatisticsDto.getRepairsCurrency;
import static pl.biketrack.repair.dto.RepairStatisticsDto.getTotalRepairCost;
import static pl.biketrack.repair.dto.RepairStatisticsDto.getTotalRepairs;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private static final String[] MONTHS_PL = {
            "Styczeń", "Luty", "Marzec", "Kwiecień",
            "Maj", "Czerwiec", "Lipiec", "Sierpień", "Wrzesień",
            "Październik", "Listopad", "Grudzień"
    };

    private final BikeRepository bikeRepository;
    private final RepairRepository repairRepository;

    public StatisticsResponse getStatistics() {
        UUID userUuid = SecurityUtils.getLoggedUserUUID();
        log.info("Start the process of getting statistics for user with UUID: [{}]", userUuid);

        Summary summary = buildSummary(userUuid);

        List<RepairsPerBikeProjection> repairsPerBikeProjections = repairRepository.findRepairsPerBikeByUserUuid(userUuid);
        List<RepairsPerBike> repairsPerBike = repairsPerBikeProjections.stream()
                                                                       .map(p -> new RepairsPerBike(p.getBikeName(), p.getRepairs()))
                                                                       .toList();

        List<AverageRepairCostPerBikeProjection> averageRepairCostPerBikeProjections = repairRepository.findAverageRepairCostPerBikeByUserUuid(userUuid);
        List<AverageRepairCostPerBike> averageRepairCostPerBike = averageRepairCostPerBikeProjections.stream()
                                                                                                     .map(p -> new AverageRepairCostPerBike(p.getBikeName(), p.getAverageCost()))
                                                                                                     .toList();

        List<RepairsThisYearPerMonth> repairsThisYearPerMonth = buildRepairsThisYearPerMonth(userUuid);

        return new StatisticsResponse(summary, repairsPerBike, averageRepairCostPerBike, repairsThisYearPerMonth);
    }

    private Summary buildSummary(UUID userUuid) {
        long totalBikes = bikeRepository.countByUserUuid(userUuid);
        List<RepairStatisticsDto> repairStatisticsDto = repairRepository.getRepairStatisticsDtoForUser(userUuid);
        long totalRepairs = getTotalRepairs(repairStatisticsDto);
        BigDecimal totalRepairsCost = getTotalRepairCost(repairStatisticsDto);
        CurrencyCode repairsCurrency = getRepairsCurrency(repairStatisticsDto);

        return new Summary(totalBikes, totalRepairs, new MoneyDto(totalRepairsCost, repairsCurrency));
    }

    private List<RepairsThisYearPerMonth> buildRepairsThisYearPerMonth(UUID userUuid) {
        List<RepairsPerMonthDtoProjection> projections = repairRepository.countRepairsPerMonthForYearByUserUuidAndYear(userUuid, Year.now().getValue());

        Map<Integer, Long> monthToRepairs = projections.stream().collect(toMap(RepairsPerMonthDtoProjection::getMonthNumber, RepairsPerMonthDtoProjection::getRepairsCount));

        List<RepairsThisYearPerMonth> result = new ArrayList<>();

        for (int month = 0; month < 12; month++) {
            long repairs = monthToRepairs.getOrDefault(month, 0L);
            result.add(new RepairsThisYearPerMonth(MONTHS_PL[month], repairs));
        }

        return result;
    }
}