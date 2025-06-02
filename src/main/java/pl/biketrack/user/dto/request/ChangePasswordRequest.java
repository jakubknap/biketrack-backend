package pl.biketrack.user.dto.request;

import pl.biketrack.validation.Password;

public record ChangePasswordRequest(

        @Password
        String password,

        @Password
        String passwordRepeat
) {}