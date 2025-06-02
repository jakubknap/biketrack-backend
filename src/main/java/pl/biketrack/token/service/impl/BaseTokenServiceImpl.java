package pl.biketrack.token.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.biketrack.authentication.dto.request.ResendTokenRequest;
import pl.biketrack.exception.dto.response.BaseResponse;
import pl.biketrack.exception.exception.ServiceException;
import pl.biketrack.token.enumerated.TokenType;
import pl.biketrack.token.model.Token;
import pl.biketrack.token.repository.TokenRepository;
import pl.biketrack.token.service.TokenService;
import pl.biketrack.user.model.User;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.nonNull;
import static pl.biketrack.common.enumerated.ResponseCode.E04000;
import static pl.biketrack.common.enumerated.ResponseCode.E04001;
import static pl.biketrack.common.enumerated.ResponseCode.E04002;
import static pl.biketrack.common.enumerated.ResponseCode.E04003;
import static pl.biketrack.common.enumerated.ResponseCode.E04004;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseTokenServiceImpl implements TokenService {

    protected final TokenRepository tokenRepository;

    protected abstract TokenType getTokenType();

    protected abstract Duration getTokenExpiration();

    @Override
    public void revokeAllUserTokensByType(UUID userUuid) {
        log.info("Revoking all valid {}S for user with UUID: [{}]", getTokenType(), userUuid);
        tokenRepository.revokeAllValidTokensByUserUuidAndTokenTypes(userUuid, List.of(getTokenType()));
    }

    @Override
    public String generateToken(User user) {
        Token token = buildToken(user);
        tokenRepository.save(token);
        return token.getToken();
    }

    @Override
    public Token getAndValidateToken(String token) {
        Token tokenEntity = findTokenWithUserOrElseThrow(token);
        validateToken(token, tokenEntity);
        return tokenEntity;
    }

    @Override
    public BaseResponse resendToken(ResendTokenRequest request) {
        throw new RuntimeException("Unsupported operation");
    }

    private Token buildToken(User user) {
        return Token.builder()
                    .token(UUID.randomUUID().toString())
                    .tokenType(getTokenType())
                    .revoked(false)
                    .expiresAt(LocalDateTime.now().plus(getTokenExpiration()))
                    .user(user)
                    .build();
    }

    private Token findTokenWithUserOrElseThrow(String token) {
        return tokenRepository.findTokenWithUserByToken(token)
                              .orElseThrow(() -> {
                                  log.error("Token: [{}] not found", token);
                                  return new ServiceException(E04000);
                              });
    }

    private void validateToken(String token, Token tokenEntity) {
        validateTokenType(tokenEntity.getTokenType(), token);
        validateTokenStatus(tokenEntity.isRevoked(), token);
        validateTokenUsage(tokenEntity.getUsedAt(), token);
        validateTokenExpiration(tokenEntity.getExpiresAt(), token);
    }

    private void validateTokenType(TokenType providedTokenType, String token) {
        TokenType expectedTokenType = getTokenType();
        if (expectedTokenType != providedTokenType) {
            log.error("Token type is invalid for this operation. Expected token type is: [{}], provided: [{}]. Token: [{}]", expectedTokenType, providedTokenType, token);
            throw new ServiceException(E04003);
        }
    }

    private void validateTokenStatus(boolean revoked, String token) {
        if (revoked) {
            log.error("Token: [{}] is revoked", token);
            throw new ServiceException(E04002);
        }
    }

    private void validateTokenUsage(LocalDateTime usageDate, String token) {
        if (nonNull(usageDate)) {
            log.error("Token: [{}] has already been used", token);
            throw new ServiceException(E04004);
        }
    }

    private void validateTokenExpiration(LocalDateTime expirationDate, String token) {
        boolean isTokenExpired = expirationDate.isBefore(LocalDateTime.now());
        if (isTokenExpired) {
            log.error("Token: [{}] is expired", token);
            throw new ServiceException(E04001);
        }
    }
}