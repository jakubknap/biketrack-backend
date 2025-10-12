package pl.biketrack.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import pl.biketrack.exception.dto.response.BaseResponse;
import pl.biketrack.user.dto.request.ChangePasswordRequest;
import pl.biketrack.user.dto.request.UpdateUserRequest;
import pl.biketrack.user.dto.response.UserDetailsResponse;
import pl.biketrack.user.service.UserService;

import static pl.biketrack.common.constant.Urls.USERS_URL;

@Slf4j
@RestController
@RequestMapping(USERS_URL)
@RequiredArgsConstructor
@Tag(name = "Zarządzanie użytkownikiem", description = "Operacje związane z użytkownikiem")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Pobierz dane zalogowanego użytkownika",
            description = "Zwraca szczegóły aktualnie zalogowanego użytkownika",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = UserDetailsResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Brak autoryzacji")
            }
    )
    @GetMapping
    public UserDetailsResponse getUserDetails() {
        return userService.getUserDetails();
    }

    @Operation(
            summary = "Aktualizuj dane użytkownika",
            description = "Pozwala zaktualizować nick lub email zalogowanego użytkownika",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = BaseResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Niepoprawne dane")
            }
    )
    @PutMapping
    public BaseResponse updateUser(@RequestBody @Valid UpdateUserRequest request) {
        return userService.updateUser(request);
    }

    @Operation(
            summary = "Zmień hasło użytkownika",
            description = "Pozwala zalogowanemu użytkownikowi zmienić hasło",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = BaseResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Niepoprawne dane")
            }
    )
    @PatchMapping("/change-password")
    public BaseResponse changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        return userService.changePassword(request);
    }
}