package pl.biketrack.token.mapper;

import lombok.experimental.UtilityClass;
import pl.biketrack.token.enumerated.TokenType;
import pl.biketrack.token.model.Token;
import pl.biketrack.user.model.User;

@UtilityClass
public class TokenMapper {

    public static Token buildToken(User user, String token, TokenType tokenType) {
        return Token.builder()
                    .token(token)
                    .tokenType(tokenType)
                    .revoked(false)
                    .user(user)
                    .build();
    }
}