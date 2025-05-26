package pl.biketrack.token.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.biketrack.exception.exception.ServiceException;
import pl.biketrack.token.enumerated.TokenType;
import pl.biketrack.token.service.impl.ActivationAccountTokenServiceImpl;

import static pl.biketrack.common.enumerated.ResponseCode.E00005;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenServiceFactory {

    private final ActivationAccountTokenServiceImpl activationAccountTokenService;

    public TokenService getTokenService(TokenType tokenType) {
        return switch (tokenType) {
            case ACCOUNT_ACTIVATION_TOKEN -> activationAccountTokenService;
            default -> {
                log.error("Unsupported token type: [{}]", tokenType);
                throw new ServiceException(E00005);
            }
        };
    }
}