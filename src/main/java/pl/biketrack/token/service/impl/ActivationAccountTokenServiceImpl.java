package pl.biketrack.token.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.biketrack.authentication.dto.request.ResendTokenRequest;
import pl.biketrack.exception.dto.response.BaseResponse;
import pl.biketrack.exception.exception.ServiceException;
import pl.biketrack.mail.impl.AccountActivationMail;
import pl.biketrack.mail.service.MailService;
import pl.biketrack.properties.FrontendProperties;
import pl.biketrack.token.enumerated.TokenType;
import pl.biketrack.token.mapper.TokenMapper;
import pl.biketrack.token.model.Token;
import pl.biketrack.token.repository.TokenRepository;
import pl.biketrack.token.service.TokenService;
import pl.biketrack.user.enumerated.UserStatus;
import pl.biketrack.user.model.User;
import pl.biketrack.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.nonNull;
import static pl.biketrack.common.enumerated.ResponseCode.E03003;
import static pl.biketrack.common.enumerated.ResponseCode.E04000;
import static pl.biketrack.common.enumerated.ResponseCode.E04001;
import static pl.biketrack.common.enumerated.ResponseCode.E04002;
import static pl.biketrack.common.enumerated.ResponseCode.E04003;
import static pl.biketrack.common.enumerated.ResponseCode.E04004;
import static pl.biketrack.common.enumerated.ResponseCode.S00000;
import static pl.biketrack.token.enumerated.TokenType.ACCOUNT_ACTIVATION_TOKEN;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivationAccountTokenServiceImpl implements TokenService {

    private static final TokenType TOKEN_TYPE = ACCOUNT_ACTIVATION_TOKEN;

    private final TokenRepository tokenRepository;
    private final TokenMapper tokenMapper;
    private final UserService userService;
    private final FrontendProperties frontendProperties;
    private final MailService mailService;

    @Override
    public void revokeAllUserTokensByType(UUID userUuid, TokenType tokenType) {
        log.info("Revoking all valid {}S for user with UUID: [{}]", tokenType, userUuid);
        tokenRepository.revokeAllValidTokensByUserUuid(userUuid, List.of(tokenType));
    }

    @Override
    public String generateToken(User user) {
        Token token = tokenMapper.buildToken(TOKEN_TYPE, user);
        tokenRepository.save(token);
        return token.getToken();
    }

    @Override
    public Token getAndValidateToken(String token) {
        Token tokenEntity = findTokenOrElseThrow(token);
        validateToken(token, tokenEntity);
        return tokenEntity;
    }

    @Override
    @Transactional
    public BaseResponse resendToken(ResendTokenRequest request) {
        User user = userService.getUserByEmail(request.email());

        validateUserStatus(request, user);

        revokeAllUserTokensByType(user.getUuid(), ACCOUNT_ACTIVATION_TOKEN);

        String activationToken = generateToken(user);
        mailService.sendMail(new AccountActivationMail(user.getEmail(), user.getNickname(), frontendProperties.prepareActivationLink(activationToken)));

        log.info("Successfully resent the token");
        return new BaseResponse(S00000);
    }

    private void validateToken(String token, Token tokenEntity) {
        validateTokenType(tokenEntity.getTokenType(), token);
        validateTokenStatus(tokenEntity.isRevoked(), token);
        validateTokenUsage(tokenEntity.getUsedAt(), token);
        validateTokenExpiration(tokenEntity.getExpiresAt(), token);
    }

    private Token findTokenOrElseThrow(String token) {
        return tokenRepository.findTokenWithUserByToken(token)
                              .orElseThrow(() -> {
                                  log.error("Token: [{}] not found", token);
                                  return new ServiceException(E04000);
                              });
    }

    private void validateTokenType(TokenType tokenType, String token) {
        if (TOKEN_TYPE != tokenType) {
            log.error("Token type is invalid for this operation. Expected token type is: [{}], provided: [{}]. Token: [{}]", TOKEN_TYPE, tokenType, token);
            throw new ServiceException(E04003);
        }
    }

    private void validateTokenStatus(boolean revoked, String token) {
        if (revoked) {
            log.error("Token: [{}] is revoked", token);
            throw new ServiceException(E04002);
        }
    }

    private void validateTokenExpiration(LocalDateTime expirationDate, String token) {
        boolean isTokenExpired = expirationDate.isBefore(LocalDateTime.now());
        if (isTokenExpired) {
            log.error("Token: [{}] is expired", token);
            throw new ServiceException(E04001);
        }
    }

    private void validateTokenUsage(LocalDateTime usageDate, String token) {
        if (nonNull(usageDate)) {
            log.error("Token: [{}] has already been used", token);
            throw new ServiceException(E04004);
        }
    }

    private void validateUserStatus(ResendTokenRequest request, User user) {
        boolean isActivationTokenRequest = TOKEN_TYPE == request.tokenType();
        boolean wasUserActivated = UserStatus.REGISTERED != user.getStatus();

        if (isActivationTokenRequest && wasUserActivated) {
            log.error("It is not possible to send a re-verification of the account because the user has already passed the verification before. User: [{}]", user.getUuid());
            throw new ServiceException(E03003);
        }
    }
}