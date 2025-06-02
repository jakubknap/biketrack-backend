package pl.biketrack.authentication.dto.request;

import jakarta.validation.constraints.NotNull;
import pl.biketrack.validation.Password;

import java.util.UUID;

public record ConfirmResetPasswordRequest(

        @NotNull
        UUID token,

        @Password
        String password,

        @Password
        String passwordRepeat
) {}