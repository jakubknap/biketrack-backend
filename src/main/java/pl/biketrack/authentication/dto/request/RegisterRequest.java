package pl.biketrack.authentication.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import static pl.biketrack.common.constant.Patterns.NICKNAME_PATTERN;
import static pl.biketrack.common.constant.Patterns.PASSWORD_PATTERN;

public record RegisterRequest(

        @NotBlank
        @Size(min = 3, max = 30)
        @Pattern(regexp = NICKNAME_PATTERN)
        String nickname,

        @Email
        @NotBlank
        String email,

        @NotBlank
        @Size(min = 8, max = 100)
        @Pattern(regexp = PASSWORD_PATTERN)
        String password
) {}