package pl.biketrack.token.service;

import pl.biketrack.authentication.dto.request.ResendTokenRequest;
import pl.biketrack.exception.dto.response.BaseResponse;
import pl.biketrack.token.enumerated.TokenType;
import pl.biketrack.token.model.Token;
import pl.biketrack.user.model.User;

import java.util.UUID;

public interface TokenService {

    void revokeAllUserTokensByType(UUID userUuid, TokenType tokenType);

    String generateToken(User user);

    Token getAndValidateToken(String token);

    BaseResponse resendToken(ResendTokenRequest request);
}