package pl.biketrack.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import pl.biketrack.exception.exception.ServiceException;
import pl.biketrack.properties.JwtProperties;
import pl.biketrack.token.dto.TokenStatusAndType;
import pl.biketrack.token.enumerated.TokenType;
import pl.biketrack.token.repository.TokenRepository;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static java.util.Objects.isNull;
import static pl.biketrack.common.enumerated.ResponseCode.E00006;
import static pl.biketrack.common.enumerated.ResponseCode.E01000;
import static pl.biketrack.common.enumerated.ResponseCode.E01001;
import static pl.biketrack.common.enumerated.ResponseCode.E01002;
import static pl.biketrack.common.enumerated.ResponseCode.E01003;
import static pl.biketrack.token.enumerated.TokenType.ACCESS_TOKEN;
import static pl.biketrack.token.enumerated.TokenType.REFRESH_TOKEN;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String BEARER = "Bearer ";
    private static final long OFFSET_NOW = 0L;

    private final JwtProperties jwtProperties;
    private final TokenRepository tokenRepository;

    public String readTokenFromHeader(HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (isNull(authHeader) || !authHeader.startsWith(BEARER)) {
            return null;
        }
        return authHeader.substring(BEARER.length());
    }

    public String extractUsernameFromToken(String token) {
        final String username = extractClaim(token, Claims::getSubject);

        if (isNull(username)) {
            log.error("Not found username in token");
            throw new ServiceException(E01002);
        }

        return username;
    }

    public void validateToken(String token, String userEmailFromUserDetails, TokenType expectedTokenType) {
        validateUsernameFromToken(token, userEmailFromUserDetails);
        validateTokenExpiration(token);
        validateTokenStatusAndExpectedType(token, expectedTokenType);
    }

    public void revokeAllUserTokens(UUID userUuid) {
        log.info("Revoking all valid tokens for user with uuid: {}", userUuid);
        tokenRepository.revokeAllValidTokensByUserUuid(userUuid, List.of(ACCESS_TOKEN, REFRESH_TOKEN));
    }

    public void revokeAllUserTokensByType(UUID userUuid, TokenType tokenType) {
        log.info("Revoking all valid {}S for user with uuid: {}", tokenType, userUuid);
        tokenRepository.revokeAllValidTokensByUserUuid(userUuid, List.of(tokenType));
    }

    public String generateAccessToken(String userEmail) {
        return generateToken(new HashMap<>(), userEmail, jwtProperties.getAccessTokenExpiration());
    }

    public String generateRefreshToken(String userEmail) {
        return generateToken(new HashMap<>(), userEmail, jwtProperties.getRefreshTokenExpiration());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        if (isNull(token)) {
            log.error("Missing token or invalid header format (missing Bearer clause)");
            throw new ServiceException(E01000);
        }

        Claims claims;

        try {
            claims = Jwts.parser()
                         .verifyWith(getSigningKey())
                         .build()
                         .parseSignedClaims(token)
                         .getPayload();

        } catch (ExpiredJwtException ex) {
            log.error("Expired token: {}", ex.getMessage(), ex);
            throw new ServiceException(E01001);

        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException ex) {
            log.error("Invalid token: {}", ex.getMessage(), ex);
            throw new ServiceException(E01002);

        } catch (Exception ex) {
            log.error("Error while parsing token: {}", ex.getMessage(), ex);
            throw new ServiceException(E00006);
        }

        if (isNull(claims)) {
            log.error("No claims found in token");
            throw new ServiceException(E01002);
        }

        return claims;
    }

    private void validateUsernameFromToken(String token, String userEmailFromUserDetails) {
        final String username = extractUsernameFromToken(token);

        if (!username.equals(userEmailFromUserDetails)) {
            log.error("Token is registered for different user");
            throw new ServiceException(E01002);
        }
    }

    private void validateTokenExpiration(String token) {
        if (isTokenExpired(token)) {
            log.error("Token expired");
            throw new ServiceException(E01001);
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(getDate(OFFSET_NOW));
    }

    private Date extractExpiration(String token) {
        final Date expirationDate = extractClaim(token, Claims::getExpiration);

        if (isNull(expirationDate)) {
            log.error("Expiration date not found in token");
            throw new ServiceException(E01002);
        }

        return expirationDate;
    }

    private void validateTokenStatusAndExpectedType(String token, TokenType expectedType) {
        final TokenStatusAndType tokenStatusAndType = getTokenStatusAndType(token);

        if (tokenStatusAndType.isRevoked()) {
            log.error("Token is revoked");
            throw new ServiceException(E01002);
        }

        final TokenType tokenType = tokenStatusAndType.tokenType();
        if (expectedType != tokenType) {
            log.error("Token type is invalid for this operation. Expected token type is: {}, provided: {}", expectedType, tokenType);
            throw new ServiceException(E01002);
        }
    }

    private TokenStatusAndType getTokenStatusAndType(String token) {
        return tokenRepository.getTokenStatusAndType(token)
                              .orElseThrow(() -> {
                                  log.error("Token not found");
                                  return new ServiceException(E01003);
                              });
    }

    private String generateToken(Map<String, Object> extraClaims, String userEmail, Duration expiration) {
        return Jwts.builder()
                   .claims(extraClaims)
                   .subject(userEmail)
                   .issuedAt(getDate(OFFSET_NOW))
                   .expiration(getDate(expiration.toMillis()))
                   .signWith(getSigningKey())
                   .compact();
    }

    private Date getDate(long offset) {
        return new Date(System.currentTimeMillis() + offset);
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecretKey()));
    }
}