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
        summary = "Pobierz UUID'y zdjęć dołączonych do naprawy",
        description = "Pozwala użytkownikowi pobrać UUID'y zdjeć dołączonych do naprawy w celu wyświetlenia ich",
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Pomyślnie pobrano UUID'y zdjęć dołączonych do naprawy",
                        content = @Content(
                                schema = @Schema(implementation = BaseResponse.class),
                                examples = @ExampleObject(
                                        value = """
                                                [
                                                    "37f4201d-9aa7-4275-89db-1724d7633797"
                                                ]
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
                        description = "Naprawa o podanym UUID nie istnieje",
                        content = @Content(
                                schema = @Schema(implementation = BaseResponse.class),
                                examples = @ExampleObject(
                                        value = """
                                                {
                                                    "status": "E06000",
                                                    "message": "Repair not found",
                                                    "httpStatus": "NOT_FOUND",
                                                    "traceId": "2d96fc7d-720a-4111-8362-b32bf2dfe97a"
                                                }
                                                """
                                )
                        )
                ),
                @ApiResponse(
                        responseCode = "422",
                        description = "Użytkownik nie jest właścicielem naprawy",
                        content = @Content(
                                schema = @Schema(implementation = BaseResponse.class),
                                examples = @ExampleObject(
                                        value = """
                                                {
                                                    "status": "E06001",
                                                    "message": "Logged in user is not the owner of the repair",
                                                    "httpStatus": "UNPROCESSABLE_ENTITY",
                                                    "traceId": "6701ff55-7290-410a-a89f-31c8b2a4e691"
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
public @interface ApiGetRepairPhotosResponse {}