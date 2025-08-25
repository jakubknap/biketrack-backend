package pl.biketrack.token.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.biketrack.authentication.dto.request.ResendTokenRequest;
import pl.biketrack.exception.dto.response.BaseResponse;
import pl.biketrack.mail.impl.ResetPasswordMail;
import pl.biketrack.mail.service.MailService;
import pl.biketrack.properties.FrontendProperties;
import pl.biketrack.properties.TokenProperties;
import pl.biketrack.token.enumerated.TokenType;
import pl.biketrack.token.model.Token;
import pl.biketrack.token.repository.TokenRepository;
import pl.biketrack.user.model.User;

import java.time.Duration;

import static pl.biketrack.common.enumerated.ResponseCode.S00000;

@Slf4j
@Service
public class ResetPasswordTokenServiceImpl extends BaseTokenServiceImpl {

    private final TokenProperties tokenProperties;
    private final FrontendProperties frontendProperties;
    private final MailService mailService;

    public ResetPasswordTokenServiceImpl(TokenRepository tokenRepository,
                                         TokenProperties tokenProperties,
                                         FrontendProperties frontendProperties,
                                         MailService mailService) {
        super(tokenRepository);
        this.tokenProperties = tokenProperties;
        this.frontendProperties = frontendProperties;
        this.mailService = mailService;
    }

    @Override
    protected TokenType getTokenType() {
        return TokenType.PASSWORD_RESET_TOKEN;
    }

    @Override
    protected Duration getTokenExpiration() {
        return tokenProperties.getPasswordResetTokenExpiration();
    }

    @Override
    @Transactional
    public BaseResponse resendToken(ResendTokenRequest request) {
        User user = validateTokenAndGetUser(request);

        revokeAllUserTokensByType(user.getUuid());

        String passwordResetToken = generateToken(user);
        mailService.sendMail(new ResetPasswordMail(user.getEmail(), user.getNickname(), frontendProperties.prepareResetPasswordLink(passwordResetToken)));

        log.info("Successfully resent token to user with UUID: [{}]", user.getUuid());
        return new BaseResponse(S00000);
    }

    private User validateTokenAndGetUser(ResendTokenRequest request) {
        String token = request.expiredToken().toString();

        Token tokenEntity = findTokenWithUserOrElseThrow(token);
        validateTokenType(tokenEntity.getTokenType(), token);
        validateTokenStatus(tokenEntity.isRevoked(), token);
        validateTokenUsage(tokenEntity.getUsedAt(), token);

        return tokenEntity.getUser();
    }
}