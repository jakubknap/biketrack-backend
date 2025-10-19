package pl.biketrack.openApi.statistics;

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
        summary = "Pobierz statystyki",
        description = "Zwraca statystyki rowerów oraz napraw użytkownika",
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Statystyki pobrane poprawnie",
                        content = @Content(
                                schema = @Schema(implementation = PageResponse.class),
                                examples = @ExampleObject(
                                        value = """
                                                {
                                                    "summary": {
                                                        "totalBikes": 1,
                                                        "totalRepairs": 1,
                                                        "totalRepairCost": {
                                                            "amount": 90.10,
                                                            "currency": "PLN"
                                                        }
                                                    },
                                                    "repairsPerBike": [
                                                        {
                                                            "bikeName": "Canyon Roadlite CF 7",
                                                            "repairs": 1
                                                        }
                                                    ],
                                                    "averageRepairCostPerBike": [
                                                        {
                                                            "bikeName": "Canyon Roadlite CF 7",
                                                            "averageCost": 90.1
                                                        }
                                                    ],
                                                    "repairsThisYearPerMonth": [
                                                        {
                                                            "month": "Styczeń",
                                                            "repairs": 0
                                                        },
                                                        {
                                                            "month": "Luty",
                                                            "repairs": 0
                                                        },
                                                        {
                                                            "month": "Marzec",
                                                            "repairs": 0
                                                        },
                                                        {
                                                            "month": "Kwiecień",
                                                            "repairs": 0
                                                        },
                                                        {
                                                            "month": "Maj",
                                                            "repairs": 0
                                                        },
                                                        {
                                                            "month": "Czerwiec",
                                                            "repairs": 0
                                                        },
                                                        {
                                                            "month": "Lipiec",
                                                            "repairs": 0
                                                        },
                                                        {
                                                            "month": "Sierpień",
                                                            "repairs": 0
                                                        },
                                                        {
                                                            "month": "Wrzesień",
                                                            "repairs": 0
                                                        },
                                                        {
                                                            "month": "Październik",
                                                            "repairs": 0
                                                        },
                                                        {
                                                            "month": "Listopad",
                                                            "repairs": 0
                                                        },
                                                        {
                                                            "month": "Grudzień",
                                                            "repairs": 0
                                                        }
                                                    ]
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
public @interface ApiGetStatisticsResponse {}