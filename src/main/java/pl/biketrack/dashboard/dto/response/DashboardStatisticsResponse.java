package pl.biketrack.dashboard.dto.response;

import lombok.Builder;
import pl.biketrack.dashboard.dto.MoneyDto;
import pl.biketrack.dashboard.dto.RecentlyAddedBikeDto;
import pl.biketrack.dashboard.dto.RecentlyAddedRepairDto;

@Builder
public record DashboardStatisticsResponse(long totalBikes,
                                          RecentlyAddedBikeDto recentlyAddedBike,
                                          long totalRepairs,
                                          MoneyDto totalRepairsCost,
                                          RecentlyAddedRepairDto recentlyAddedRepair) {}