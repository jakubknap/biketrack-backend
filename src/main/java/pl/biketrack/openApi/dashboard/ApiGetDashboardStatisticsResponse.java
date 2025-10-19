package pl.biketrack.openApi.dashboard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import pl.biketrack.common.dto.PageResponse;
import pl.biketrack.exception.dto.response.BaseResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Operation(
        summary = "Pobierz dane pulpitu",
        description = "Zwraca statystyki i zestawienie aktywności użytkownika – ilość rowerów, napraw, ostatni dodany rower, naprawa",
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Dane pulpitu pobrane poprawnie",
                        content = @Content(
                                schema = @Schema(implementation = PageResponse.class),
                                examples = @ExampleObject(
                                        value = """
                                                {
                                                    "totalBikes": 1,
                                                    "recentlyAddedBike": {
                                                        "name": "Canyon Roadlite CF 7",
                                                        "uuid": "bc20dc44-1ea8-49a8-81e3-099ca6009b4e"
                                                    },
                                                    "totalRepairs": 1,
                                                    "totalRepairsCost": {
                                                        "amount": 90.10,
                                                        "currency": "PLN"
                                                    },
                                                    "recentlyAddedRepair": {
                                                        "uuid": "95c07c1a-9994-4989-bb9d-e25e685057a4",
                                                        "title": "Serwis napędu i wymiana łańcucha",
                                                        "repairCost": {
                                                            "amount": 90.10,
                                                            "currency": "PLN"
                                                        }
                                                    }
                                                }
                                                """
                                )
                        )
                ),
                @ApiResponse(
                        responseCode = "401",
                        description = "Użytkownik nieuwierzytelniony",
                        content = @Content(
                                schema = @Schema(implementation = BaseResponse.class),
                                examples = @ExampleObject(
                                        value = """
                                                {
                                                    "status": "E01002",
                                                    "message": "Invalid token",
                                                    "httpStatus": "UNAUTHORIZED",
                                                    "traceId": "0c501596-9e18-4bc3-b0fc-9ae3dc9e5f31"
                                                }
                                                """
                                )
                        )
                ),
                @ApiResponse(
                        responseCode = "500",
                        description = "Wystąpił wewnętrzny błąd serwera",
                        content = @Content(
                                schema = @Schema(implementation = BaseResponse.class),
                                examples = @ExampleObject(
                                        value = """
                                                {
                                                    "status": "E00006",
                                                    "message": "Internal server error",
                                                    "httpStatus": "INTERNAL_SERVER_ERROR",
                                                    "traceId": "2a7d17a1-c10d-49cb-a640-83185a19db6d"
                                                }
                                                """
                                )
                        )
                )
        }
)
public @interface ApiGetDashboardStatisticsResponse {}