package pl.biketrack.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
import pl.biketrack.token.model.Token;
import pl.biketrack.token.repository.TokenRepository;

import java.util.UUID;

import static java.util.Objects.isNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {

    private final JwtService jwtService;
    private final TokenRepository tokenRepository;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        log.info("Logout process started");

        final String jwt = jwtService.readJwtFromHeader(request);

        if (isNull(jwt)) {
            log.info("No authorization header or invalid authorization header. Logout skipped");
            return;
        }

        Token token = tokenRepository.findByTokenWithUser(jwt)
                                     .orElse(null);

        if (isNull(token)) {
            log.info("Token not found in database. Logout skipped");
            return;
        }

        UUID userUuid = token.getUser()
                             .getUuid();

        jwtService.revokeAllUserTokens(userUuid);
        SecurityContextHolder.clearContext();

        log.info("Logout successful for user with uuid: {}", userUuid);
    }
}