package pl.biketrack.authentication.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.biketrack.authentication.dto.request.ConfirmResetPasswordRequest;
import pl.biketrack.authentication.dto.request.LoginRequest;
import pl.biketrack.authentication.dto.request.RegisterRequest;
import pl.biketrack.authentication.dto.request.ResendTokenRequest;
import pl.biketrack.authentication.dto.request.ResetPasswordRequest;
import pl.biketrack.authentication.dto.response.AuthenticationResponse;
import pl.biketrack.authentication.service.AuthenticationService;
import pl.biketrack.authentication.service.PasswordService;
import pl.biketrack.exception.dto.response.BaseResponse;
import pl.biketrack.token.enumerated.TokenType;
import pl.biketrack.token.service.TokenServiceFactory;

import java.util.UUID;

import static pl.biketrack.common.constant.Urls.AUTH_URL;
import static pl.biketrack.util.MaskingUtil.maskEmail;

@Slf4j
@RestController
@RequestMapping(AUTH_URL)
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final PasswordService passwordService;
    private final TokenServiceFactory tokenServiceFactory;

    @PostMapping("/register")
    public BaseResponse register(@Valid @RequestBody RegisterRequest request) {
        log.info("Start user registration with e-mail: [{}]", maskEmail(request.email()));
        return authenticationService.register(request);
    }

    @GetMapping("/activate-account/{activationToken}")
    public BaseResponse activateAccount(@PathVariable UUID activationToken) {
        log.info("Start user account activation for token: [{}]", activationToken);
        return authenticationService.activateAccount(activationToken);
    }

    @PostMapping("/authenticate")
    public AuthenticationResponse authenticate(@Valid @RequestBody LoginRequest request) {
        log.info("Start user authentication with e-mail: [{}]", maskEmail(request.email()));
        return authenticationService.authenticate(request);
    }

    @PostMapping("/refresh-token")
    public AuthenticationResponse refreshToken(HttpServletRequest request) {
        log.info("Start refreshing user token");
        return authenticationService.refreshToken(request);
    }

    @PostMapping("/password-reset/request")
    public BaseResponse resetPasswordRequest(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Start password recovery process for user with e-mail: [{}]", maskEmail(request.email()));
        return passwordService.resetPasswordRequest(request);
    }

    @PostMapping("/password-reset/confirm")
    public BaseResponse confirmResetPassword(@Valid @RequestBody ConfirmResetPasswordRequest request) {
        log.info("Start password reset process for token: [{}]", request.token());
        return passwordService.confirmResetPassword(request);
    }

    @PostMapping("/resend-token")
    public BaseResponse resendToken(@Valid @RequestBody ResendTokenRequest request) {
        TokenType tokenType = request.tokenType();
        log.info("Start resending user token: [{}] for user with e-mail: [{}]", tokenType, maskEmail(request.email()));
        return tokenServiceFactory.getTokenService(tokenType)
                                  .resendToken(request);
    }
}