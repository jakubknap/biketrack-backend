package pl.biketrack.authentication.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.biketrack.authentication.dto.request.ConfirmResetPasswordRequest;
import pl.biketrack.authentication.dto.request.ResetPasswordRequest;
import pl.biketrack.authentication.service.PasswordService;
import pl.biketrack.common.enumerated.ResponseCode;
import pl.biketrack.exception.dto.response.BaseResponse;
import pl.biketrack.exception.exception.ServiceException;
import pl.biketrack.mail.impl.ResetPasswordMail;
import pl.biketrack.mail.service.MailService;
import pl.biketrack.properties.FrontendProperties;
import pl.biketrack.token.enumerated.TokenType;
import pl.biketrack.token.model.Token;
import pl.biketrack.token.repository.TokenRepository;
import pl.biketrack.token.service.TokenService;
import pl.biketrack.token.service.TokenServiceFactory;
import pl.biketrack.user.model.User;
import pl.biketrack.user.repository.UserRepository;
import pl.biketrack.user.service.UserService;

import java.time.LocalDateTime;

import static pl.biketrack.common.enumerated.ResponseCode.E03004;
import static pl.biketrack.common.enumerated.ResponseCode.E03005;
import static pl.biketrack.common.enumerated.ResponseCode.S00000;
import static pl.biketrack.util.MaskingUtil.maskEmail;
import static pl.biketrack.util.StringUtil.notEquals;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordServiceImpl implements PasswordService {

    private final UserService userService;
    private final TokenServiceFactory tokenServiceFactory;
    private final MailService mailService;
    private final FrontendProperties frontendProperties;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Override
    public BaseResponse resetPasswordRequest(ResetPasswordRequest request) {
        User user;

        try {
            user = userService.getUserByEmail(request.email());
        } catch (ServiceException ex) {
            log.warn("Password reset requested for non-existing account [{}]. Responding with generic message for security reasons.", maskEmail(request.email()));
            return new BaseResponse(ResponseCode.S00000);
        }

        sendPasswordResetLink(user);

        log.info("Successfully send reset password link to user with UUID: [{}]", user.getUuid());
        return new BaseResponse(ResponseCode.S00000);
    }

    @Override
    public BaseResponse confirmResetPassword(ConfirmResetPasswordRequest request) {
        Token tokenEntity = tokenServiceFactory.getTokenService(TokenType.PASSWORD_RESET_TOKEN)
                                               .getAndValidateToken(request.token().toString());

        validateResetPasswordRequest(request.password(), request.passwordRepeat(), tokenEntity.getUser());

        updatePassword(tokenEntity, request.password());

        log.info("Successfully reset password for user with UUID: [{}]", tokenEntity.getUser().getUuid());
        return new BaseResponse(S00000);
    }

    private void sendPasswordResetLink(User user) {
        TokenService tokenService = tokenServiceFactory.getTokenService(TokenType.PASSWORD_RESET_TOKEN);

        tokenService.revokeAllUserTokensByType(user.getUuid());

        String passwordResetToken = tokenService.generateToken(user);

        mailService.sendMail(new ResetPasswordMail(user.getEmail(), user.getNickname(), frontendProperties.prepareResetPasswordLink(passwordResetToken)));
    }

    private void validateResetPasswordRequest(String password, String passwordRepeat, User user) {
        if (notEquals(password, passwordRepeat)) {
            log.error("Password does not match password repeated");
            throw new ServiceException(E03004);
        }

        if (passwordEncoder.matches(password, user.getPassword())) {
            log.error("The password is the same as the user's current password");
            throw new ServiceException(E03005);
        }
    }

    private void updatePassword(Token tokenEntity, String newPassword) {
        User user = tokenEntity.getUser();

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenEntity.setUsedAt(LocalDateTime.now());
        tokenRepository.save(tokenEntity);
    }
}