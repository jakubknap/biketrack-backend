package pl.biketrack.authentication.dto.request;

import jakarta.validation.constraints.NotNull;
import pl.biketrack.token.enumerated.TokenType;
import pl.biketrack.validation.Email;

public record ResendTokenRequest(

        @NotNull
        TokenType tokenType,

        @Email
        String email
) {}