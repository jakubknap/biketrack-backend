package pl.biketrack.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import pl.biketrack.properties.JwtProperties;
import pl.biketrack.token.enumerated.TokenType;
import pl.biketrack.token.model.Token;
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
import static java.util.Objects.nonNull;
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

    public String readJwtFromHeader(HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (isNull(authHeader) || !authHeader.startsWith(BEARER)) {
            return null;
        }
        return authHeader.substring(BEARER.length());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, String userEmailFromUserDetails, TokenType expectedTokenType) {
        final String username = extractUsername(token);

        return nonNull(username) &&
               username.equals(userEmailFromUserDetails) &&
               !isTokenExpired(token) &&
               isTokenNotRejectedAndOfExpectedType(token, expectedTokenType);
    }

    public void revokeAllUserTokens(UUID userUuid) {
        log.info("Revoking all valid tokens for user with uuid: {}", userUuid);
        List<Token> tokensToRevoke = tokenRepository.findAllValidTokensForUserByType(userUuid, List.of(ACCESS_TOKEN, REFRESH_TOKEN));

        if (tokensToRevoke.isEmpty()) {
            return;
        }

        tokensToRevoke.forEach(token -> token.setRevoked(true));
        tokenRepository.saveAll(tokensToRevoke);
    }

    public void revokeAllUserTokensByType(UUID userUuid, TokenType tokenType) {
        log.info("Revoking all valid {}S for user with uuid: {}", tokenType, userUuid);

        List<Token> tokensToRevoke = tokenRepository.findAllValidTokensForUserByType(userUuid, List.of(tokenType));

        if (tokensToRevoke.isEmpty()) {
            return;
        }

        tokensToRevoke.forEach(token -> token.setRevoked(true));
        tokenRepository.saveAll(tokensToRevoke);
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
        return Jwts.parser()
                   .verifyWith(getSigningKey())
                   .build()
                   .parseSignedClaims(token)
                   .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(getDate(OFFSET_NOW));
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private boolean isTokenNotRejectedAndOfExpectedType(String token, TokenType expectedType) {
        return tokenRepository.findByToken(token)
                              .map(t -> !t.isRevoked() && t.getTokenType() == expectedType)
                              .orElse(false);
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