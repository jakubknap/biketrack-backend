package pl.biketrack.token.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.biketrack.properties.TokenProperties;
import pl.biketrack.token.enumerated.TokenType;
import pl.biketrack.token.model.Token;
import pl.biketrack.user.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TokenMapper {

    private final TokenProperties tokenProperties;

    public Token buildJwtToken(User user, String token, TokenType tokenType) {
        return Token.builder()
                    .token(token)
                    .tokenType(tokenType)
                    .revoked(false)
                    .user(user)
                    .build();
    }

    public Token buildToken(TokenType tokenType, User user) {
        return Token.builder()
                    .token(UUID.randomUUID().toString())
                    .tokenType(tokenType)
                    .revoked(false)
                    .expiresAt(LocalDateTime.now().plus(tokenProperties.getAccountActivationTokenExpiration()))
                    .user(user)
                    .build();
    }
}