package pl.biketrack.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import static pl.biketrack.common.constant.Patterns.PASSWORD_PATTERN;

public record ChangePasswordRequest(

        @NotBlank
        @Size(min = 8, max = 100)
        @Pattern(regexp = PASSWORD_PATTERN)
        String password,

        @NotBlank
        @Size(min = 8, max = 100)
        @Pattern(regexp = PASSWORD_PATTERN)
        String passwordRepeat
) {}