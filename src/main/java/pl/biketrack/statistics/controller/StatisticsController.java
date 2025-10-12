package pl.biketrack.statistics.controller;

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

    @Operation(
            summary = "Pobierz statystyki rowerów i napraw użytkownika",
            description = "Zwraca dane statystyczne użytkownika takie jak liczba rowerów, napraw, naprawy w roku itp.",
            responses = {@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = StatisticsResponse.class)))}
    )
    @GetMapping
    public StatisticsResponse getStatistics() {
        return statisticsService.getStatistics();
    }
}