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
        summary = "Edytuj rower",
        description = "Pozwala użytkownikowi edytować istniejący rower",
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Rower został zaktualizowany",
                        content = @Content(
                                schema = @Schema(implementation = BaseResponse.class),
                                examples = @ExampleObject(
                                        value = """
                                                {
                                                    "status": "S00000",
                                                    "message": "Success",
                                                    "httpStatus": "OK"
                                                }
                                                """
                                )
                        )
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "Niepoprawne dane wejściowe",
                        content = @Content(
                                schema = @Schema(implementation = BaseResponse.class),
                                examples = @ExampleObject(
                                        value = """
                                                {
                                                    "status": "E00000",
                                                    "message": "Bad Request",
                                                    "httpStatus": "BAD_REQUEST",
                                                    "traceId": "99cc1ac5-478f-4271-8110-d2e840add3ea",
                                                    "errors": [
                                                        {
                                                            "field": "bikeUuid",
                                                            "message": "nie może mieć wartości null"
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
                        responseCode = "404",
                        description = "Rower o podanym UUID nie istnieje",
                        content = @Content(
                                schema = @Schema(implementation = BaseResponse.class),
                                examples = @ExampleObject(
                                        value = """
                                                {
                                                    "status": "E05000",
                                                    "message": "Bike not found",
                                                    "httpStatus": "NOT_FOUND",
                                                    "traceId": "2a3e5c46-f36f-4ac1-8953-98e4b53e694d"
                                                }
                                                """
                                )
                        )
                ),
                @ApiResponse(
                        responseCode = "422",
                        description = "Użytkownik nie jest właścicielem roweru",
                        content = @Content(
                                schema = @Schema(implementation = BaseResponse.class),
                                examples = @ExampleObject(
                                        value = """
                                                {
                                                    "status": "E05001",
                                                    "message": "Logged in user is not the owner of the bike",
                                                    "httpStatus": "UNPROCESSABLE_ENTITY",
                                                    "traceId": "0cb659b3-2a47-4d75-b876-574975b67760"
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
public @interface ApiUpdateBikeResponse {}