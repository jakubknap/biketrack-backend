package pl.biketrack.statistics.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.biketrack.openApi.statistics.ApiGetStatisticsResponse;
import pl.biketrack.statistics.dto.response.StatisticsResponse;
import pl.biketrack.statistics.service.StatisticsService;

import static pl.biketrack.common.constant.Urls.STATISTICS_URL;

@Slf4j
@RestController
@RequestMapping(STATISTICS_URL)
@RequiredArgsConstructor
@Tag(name = "Statystyki", description = "Statystyki rowerów i napraw użytkownika")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping
    @ApiGetStatisticsResponse
    public StatisticsResponse getStatistics() {
        return statisticsService.getStatistics();
    }
}