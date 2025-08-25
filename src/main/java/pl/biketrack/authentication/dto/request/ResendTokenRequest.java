package pl.biketrack.authentication.dto.request;

import jakarta.validation.constraints.NotNull;
import pl.biketrack.token.enumerated.TokenType;

import java.util.UUID;

public record ResendTokenRequest(

        @NotNull
        TokenType tokenType,

        @NotNull
        UUID expiredToken
) {}