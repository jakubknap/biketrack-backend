package pl.biketrack.authentication.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pl.biketrack.token.enumerated.TokenType;

public record ResendTokenRequest(

        @NotNull
        TokenType tokenType,

        @Email
        @NotBlank
        String email
) {}