package pl.biketrack.dashboard.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.biketrack.dashboard.dto.response.DashboardStatisticsResponse;
import pl.biketrack.dashboard.service.DashboardService;

import static pl.biketrack.common.constant.Urls.DASHBOARD_URL;

@Slf4j
@RestController
@RequestMapping(DASHBOARD_URL)
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/statistics")
    public DashboardStatisticsResponse getDashboardStatistics() {
        return dashboardService.getDashboardStatistics();
    }
}