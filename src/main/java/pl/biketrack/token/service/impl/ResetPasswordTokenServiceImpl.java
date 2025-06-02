package pl.biketrack.token.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.biketrack.properties.TokenProperties;
import pl.biketrack.token.enumerated.TokenType;
import pl.biketrack.token.repository.TokenRepository;

import java.time.Duration;

@Slf4j
@Service
public class ResetPasswordTokenServiceImpl extends BaseTokenServiceImpl {

    private final TokenProperties tokenProperties;

    public ResetPasswordTokenServiceImpl(TokenRepository tokenRepository, TokenProperties tokenProperties) {
        super(tokenRepository);
        this.tokenProperties = tokenProperties;
    }

    @Override
    protected TokenType getTokenType() {
        return TokenType.PASSWORD_RESET_TOKEN;
    }

    @Override
    protected Duration getTokenExpiration() {
        return tokenProperties.getPasswordResetTokenExpiration();
    }
}