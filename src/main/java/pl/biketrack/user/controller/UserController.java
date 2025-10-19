package pl.biketrack.user.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.biketrack.exception.dto.response.BaseResponse;
import pl.biketrack.openApi.user.ApiChangePasswordResponse;
import pl.biketrack.openApi.user.ApiGetUserUserDetailsResponse;
import pl.biketrack.openApi.user.ApiUpdateUserResponse;
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

    @GetMapping
    @ApiGetUserUserDetailsResponse
    public UserDetailsResponse getUserDetails() {
        return userService.getUserDetails();
    }

    @PutMapping
    @ApiUpdateUserResponse
    public BaseResponse updateUser(@RequestBody @Valid UpdateUserRequest request) {
        return userService.updateUser(request);
    }

    @PatchMapping("/change-password")
    @ApiChangePasswordResponse
    public BaseResponse changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        return userService.changePassword(request);
    }
}