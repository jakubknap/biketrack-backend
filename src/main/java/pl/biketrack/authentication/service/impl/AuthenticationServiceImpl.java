package pl.biketrack.authentication.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.biketrack.authentication.dto.request.LoginRequest;
import pl.biketrack.authentication.dto.request.RegisterRequest;
import pl.biketrack.authentication.dto.response.AuthenticationResponse;
import pl.biketrack.authentication.service.AuthenticationService;
import pl.biketrack.exception.dto.response.BaseResponse;
import pl.biketrack.exception.exception.ServiceException;
import pl.biketrack.mail.impl.AccountActivationMail;
import pl.biketrack.mail.service.MailService;
import pl.biketrack.properties.FrontendProperties;
import pl.biketrack.security.JwtService;
import pl.biketrack.token.dto.TokenPairDto;
import pl.biketrack.token.enumerated.TokenType;
import pl.biketrack.token.model.Token;
import pl.biketrack.token.repository.TokenRepository;
import pl.biketrack.token.service.TokenServiceFactory;
import pl.biketrack.user.enumerated.UserStatus;
import pl.biketrack.user.mapper.UserMapper;
import pl.biketrack.user.model.User;
import pl.biketrack.user.repository.UserRepository;
import pl.biketrack.user.service.UserService;
import pl.biketrack.util.MaskingUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static pl.biketrack.common.enumerated.ResponseCode.E00006;
import static pl.biketrack.common.enumerated.ResponseCode.E02000;
import static pl.biketrack.common.enumerated.ResponseCode.E02001;
import static pl.biketrack.common.enumerated.ResponseCode.E02002;
import static pl.biketrack.common.enumerated.ResponseCode.E02003;
import static pl.biketrack.common.enumerated.ResponseCode.E02004;
import static pl.biketrack.common.enumerated.ResponseCode.E03000;
import static pl.biketrack.common.enumerated.ResponseCode.E03001;
import static pl.biketrack.common.enumerated.ResponseCode.E03002;
import static pl.biketrack.common.enumerated.ResponseCode.E03003;
import static pl.biketrack.common.enumerated.ResponseCode.S00000;
import static pl.biketrack.common.enumerated.ResponseCode.S00003;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserService userService;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    private final AuthenticationManager authenticationManager;
    private final TokenServiceFactory tokenServiceFactory;
    private final MailService mailService;
    private final FrontendProperties frontendProperties;

    @Override
    @Transactional
    public BaseResponse register(RegisterRequest request) {
        String email = request.email();

        if (userRepository.existsByEmailOrNickname(email, request.nickname())) {
            log.error("User with this e-mail or nickname already exists");
            throw new ServiceException(E03000);
        }

        User user = userMapper.mapToUserEntity(request);
        userRepository.save(user);

        sendActivationLink(user);

        log.info("User with e-mail: [{}] has been successfully registered. Assigned UUID: {}", MaskingUtil.maskEmail(email), user.getUuid());
        return new BaseResponse(S00003);
    }

    @Override
    @Transactional
    public BaseResponse activateAccount(UUID token) {
        Token tokenEntity = tokenServiceFactory.getTokenService(TokenType.ACCOUNT_ACTIVATION_TOKEN)
                                               .getAndValidateToken(token.toString());

        activateUser(tokenEntity);

        log.info("Successfully activated user with UUID: [{}]", tokenEntity.getUser().getUuid());
        return new BaseResponse(S00000);
    }

    @Override
    @Transactional
    public AuthenticationResponse authenticate(LoginRequest request) {
        User user = tryAuthenticateUser(request);

        TokenPairDto generatedTokens = generateTokens(user);

        log.info("Successfully authenticated user with UUID: [{}]", user.getUuid());
        return new AuthenticationResponse(generatedTokens.accessToken(), generatedTokens.refreshToken());
    }

    @Override
    @Transactional
    public AuthenticationResponse refreshToken(HttpServletRequest request) {
        final String refreshToken = jwtService.readTokenFromHeader(request);

        final User user = extractUserFromRefreshToken(refreshToken);
        final String userEmail = user.getEmail();

        jwtService.validateToken(refreshToken, userEmail, TokenType.REFRESH_TOKEN);
        jwtService.revokeAllUserJwtAccessTokens(user.getUuid());

        Token accessToken = jwtService.buildJwtTokenEntity(user, jwtService.generateAccessToken(userEmail), TokenType.ACCESS_TOKEN);
        tokenRepository.save(accessToken);

        log.info("Successfully refreshed access token for user with UUID: [{}]", user.getUuid());
        return new AuthenticationResponse(accessToken.getToken(), refreshToken);
    }

    private void sendActivationLink(User user) {
        String activationToken = tokenServiceFactory.getTokenService(TokenType.ACCOUNT_ACTIVATION_TOKEN)
                                                    .generateToken(user);
        mailService.sendMailAsync(new AccountActivationMail(user.getEmail(), user.getNickname(), frontendProperties.prepareAccountActivationLink(activationToken)));
    }

    private void activateUser(Token tokenEntity) {
        User user = tokenEntity.getUser();

        validateUserStatus(user);

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        tokenEntity.setUsedAt(LocalDateTime.now());
        tokenRepository.save(tokenEntity);
    }

    private void validateUserStatus(User user) {
        if (UserStatus.REGISTERED != user.getStatus()) {
            log.error("The user with UUID: [{}] has already been activated before. Re-verification is not possible.", user.getUuid());
            throw new ServiceException(E03003);
        }
    }

    private User tryAuthenticateUser(LoginRequest request) {
        String email = request.email();
        User user = userService.getUserByEmail(email);

        if (user.getStatus().isNotActive()) {
            log.error("User with UUID: [{}] does not have an active account. Cannot authenticate", user.getUuid());
            throw new ServiceException(E03002);
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        } catch (BadCredentialsException ex) {
            log.error("Bad credentials: {}", ex.getMessage(), ex);
            throw new ServiceException(E02000);

        } catch (UsernameNotFoundException ex) {
            log.error("User not found: {}", ex.getMessage(), ex);
            throw new ServiceException(E03001);

        } catch (DisabledException ex) {
            log.error("User is disabled: {}", ex.getMessage(), ex);
            throw new ServiceException(E02001);

        } catch (LockedException ex) {
            log.error("User is locked: {}", ex.getMessage(), ex);
            throw new ServiceException(E02002);

        } catch (AccountExpiredException ex) {
            log.error("Account expired: {}", ex.getMessage(), ex);
            throw new ServiceException(E02003);

        } catch (CredentialsExpiredException ex) {
            log.error("Credentials expired: {}", ex.getMessage(), ex);
            throw new ServiceException(E02004);

        } catch (Exception ex) {
            log.error("Unexpected error during authentication: {}", ex.getMessage(), ex);
            throw new ServiceException(E00006);
        }

        return user;
    }

    private TokenPairDto generateTokens(User user) {
        String email = user.getEmail();

        jwtService.revokeAllUserJwtTokens(user.getUuid());

        Token accessToken = jwtService.buildJwtTokenEntity(user, jwtService.generateAccessToken(email), TokenType.ACCESS_TOKEN);
        Token refreshToken = jwtService.buildJwtTokenEntity(user, jwtService.generateRefreshToken(email), TokenType.REFRESH_TOKEN);

        tokenRepository.saveAll(List.of(accessToken, refreshToken));
        return new TokenPairDto(accessToken.getToken(), refreshToken.getToken());
    }

    private User extractUserFromRefreshToken(String refreshToken) {
        String userEmail = jwtService.extractUsernameFromToken(refreshToken);
        return userService.getUserByEmail(userEmail);
    }
}