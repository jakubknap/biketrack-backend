package pl.biketrack.openApi.bike;

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
        summary = "Pobierz listę rowerów",
        description = "Pozwala pobrać listę rowerów dodanych przez użytkownika",
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Pomyślnie pobrano listę rowerów",
                        content = @Content(
                                schema = @Schema(implementation = BaseResponse.class),
                                examples = @ExampleObject(
                                        value = """
                                                {
                                                    "content": [
                                                        {
                                                            "uuid": "bc20dc44-1ea8-49a8-81e3-099ca6009b4e",
                                                            "name": "Canyon Roadlite CF 7",
                                                            "photo": null
                                                        }
                                                    ],
                                                    "pageable": {
                                                        "pageNumber": 0,
                                                        "pageSize": 5,
                                                        "sort": {
                                                            "empty": false,
                                                            "sorted": true,
                                                            "unsorted": false
                                                        },
                                                        "offset": 0,
                                                        "paged": true,
                                                        "unpaged": false
                                                    },
                                                    "last": true,
                                                    "totalElements": 1,
                                                    "totalPages": 1,
                                                    "size": 5,
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
public @interface ApiGetBikeListResponse {}