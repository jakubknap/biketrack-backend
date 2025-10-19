package pl.biketrack.openApi.repair;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import pl.biketrack.exception.dto.response.BaseResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Operation(
        summary = "Pobierz listę napraw",
        description = "Pozwala pobrać listę napraw dodanych przez użytkownika",
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Pomyślnie pobrano listę napraw",
                        content = @Content(
                                schema = @Schema(implementation = BaseResponse.class),
                                examples = @ExampleObject(
                                        value = """
                                                {
                                                    "content": [
                                                        {
                                                            "uuid": "5364b14b-526c-4a3a-88af-a6eac4dce7c0",
                                                            "title": "Serwis napędu i wymiana łańcucha",
                                                            "createdDate": "2025-10-19T22:57:59.763316",
                                                            "cost": {
                                                                "amount": 90.10,
                                                                "currency": "PLN"
                                                            },
                                                            "bike": {
                                                                "uuid": "bc20dc44-1ea8-49a8-81e3-099ca6009b4e",
                                                                "name": "Canyon Roadlite CF 7"
                                                            }
                                                        }
                                                    ],
                                                    "pageable": {
                                                        "pageNumber": 0,
                                                        "pageSize": 1,
                                                        "sort": {
                                                            "empty": false,
                                                            "sorted": true,
                                                            "unsorted": false
                                                        },
                                                        "offset": 0,
                                                        "paged": true,
                                                        "unpaged": false
                                                    },
                                                    "last": false,
                                                    "totalElements": 2,
                                                    "totalPages": 2,
                                                    "size": 1,
                                                    "number": 0,
                                                    "sort": {
                                                        "empty": false,
                                                        "sorted": true,
                                                        "unsorted": false
                                                    },
                                                    "first": true,
                                                    "numberOfElements": 1,
                                                    "empty": false
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
public @interface ApiGetRepairListResponse {}