package pl.biketrack.dashboard.service;

import pl.biketrack.dashboard.dto.response.DashboardStatisticsResponse;

public interface DashboardService {

    DashboardStatisticsResponse getDashboardStatistics();
}