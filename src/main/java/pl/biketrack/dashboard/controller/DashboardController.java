package pl.biketrack.dashboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Panel główny", description = "Dane podsumowujące konto użytkownika")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(
            summary = "Pobierz dane pulpitu",
            description = "Zwraca statystyki i zestawienie aktywności użytkownika – ilość rowerów, napraw, ostatni dodany rower, naprawa",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Dane pulpitu",
                            content = @Content(schema = @Schema(implementation = DashboardStatisticsResponse.class)))
            }
    )
    @GetMapping("/statistics")
    public DashboardStatisticsResponse getDashboardStatistics() {
        return dashboardService.getDashboardStatistics();
    }
}