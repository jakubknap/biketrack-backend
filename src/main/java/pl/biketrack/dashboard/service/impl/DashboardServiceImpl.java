package pl.biketrack.dashboard.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.biketrack.bike.repository.BikeRepository;
import pl.biketrack.dashboard.dto.MoneyDto;
import pl.biketrack.dashboard.dto.RecentlyAddedBikeDto;
import pl.biketrack.dashboard.dto.RecentlyAddedRepairDto;
import pl.biketrack.dashboard.dto.response.DashboardStatisticsResponse;
import pl.biketrack.dashboard.service.DashboardService;
import pl.biketrack.repair.dto.RepairStatisticsDto;
import pl.biketrack.repair.repository.RepairRepository;
import pl.biketrack.security.util.SecurityUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static pl.biketrack.repair.dto.RepairStatisticsDto.getRepairsCurrency;
import static pl.biketrack.repair.dto.RepairStatisticsDto.getTotalRepairCost;
import static pl.biketrack.repair.dto.RepairStatisticsDto.getTotalRepairs;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final BikeRepository bikeRepository;
    private final RepairRepository repairRepository;

    @Override
    public DashboardStatisticsResponse getDashboardStatistics() {
        UUID userUuid = SecurityUtils.getLoggedUserUUID();
        log.info("Start the process of getting dashboard statistics for user with UUID: [{}]", userUuid);

        long totalBikes = bikeRepository.countByUserUuid(userUuid);

        RecentlyAddedBikeDto recentlyAddedBike = null;
        if (totalBikes > 0) {
            recentlyAddedBike = bikeRepository.findRecentlyAddedBikeByUserUuid(userUuid)
                                              .stream()
                                              .findFirst()
                                              .orElse(null);
        }

        List<RepairStatisticsDto> repairStatisticsDto = repairRepository.getRepairStatisticsDtoForUser(userUuid);

        long totalRepairs = getTotalRepairs(repairStatisticsDto);

        BigDecimal totalRepairsCost = getTotalRepairCost(repairStatisticsDto);

        RecentlyAddedRepairDto recentlyAddedRepair = null;
        if (totalRepairs > 0) {
            recentlyAddedRepair = repairRepository.findRecentlyAddedRepairByUserUuid(userUuid);
        }

        return DashboardStatisticsResponse.builder()
                                          .totalBikes(totalBikes)
                                          .recentlyAddedBike(recentlyAddedBike)
                                          .totalRepairs(totalRepairs)
                                          .totalRepairsCost(new MoneyDto(totalRepairsCost, getRepairsCurrency(repairStatisticsDto)))
                                          .recentlyAddedRepair(recentlyAddedRepair)
                                          .build();
    }
}