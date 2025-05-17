package pl.biketrack.authentication.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank
        @Size(min = 3, max = 30)
        @Pattern(regexp = "^[\\p{L}0-9]+$")
        String nickname,

        @Email
        @NotBlank
        String email,

        @NotBlank
        @Size(min = 8, max = 100)
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.\\-_])[A-Za-z\\d@$!%*?&.\\-_]{8,}$")
        String password
) {}