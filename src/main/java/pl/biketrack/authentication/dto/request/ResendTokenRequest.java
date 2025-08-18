package pl.biketrack.authentication.dto.request;

import jakarta.validation.constraints.NotNull;
import pl.biketrack.token.enumerated.TokenType;
import pl.biketrack.validation.EmailOrToken;
import pl.biketrack.validation.OptionalEmail;

import java.util.UUID;

@EmailOrToken
public record ResendTokenRequest(

        @NotNull
        TokenType tokenType,

        @OptionalEmail
        String email,

        UUID expiredToken
) {}