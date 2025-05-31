package pl.biketrack.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import static pl.biketrack.common.constant.Patterns.NICKNAME_PATTERN;

public record UpdateUserRequest(

        @NotBlank
        @Size(min = 3, max = 30)
        @Pattern(regexp = NICKNAME_PATTERN)
        String nickname,

        @Email
        @NotBlank
        String email
) {}