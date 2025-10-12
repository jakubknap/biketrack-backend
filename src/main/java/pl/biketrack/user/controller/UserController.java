package pl.biketrack.user.controller;

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
import pl.biketrack.user.dto.request.ChangePasswordRequest;
import pl.biketrack.user.dto.request.UpdateUserRequest;
import pl.biketrack.user.dto.response.UserDetailsResponse;
import pl.biketrack.user.service.UserService;

import static pl.biketrack.common.constant.Urls.USERS_URL;

@Slf4j
@RestController
@RequestMapping(USERS_URL)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public UserDetailsResponse getUserDetails() {
        return userService.getUserDetails();
    }

    @PutMapping
    public BaseResponse updateUser(@RequestBody @Valid UpdateUserRequest request) {
        return userService.updateUser(request);
    }

    @PatchMapping("/change-password")
    public BaseResponse changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        return userService.changePassword(request);
    }
}