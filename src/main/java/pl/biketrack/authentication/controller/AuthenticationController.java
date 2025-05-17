package pl.biketrack.authentication.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.biketrack.authentication.dto.request.LoginRequest;
import pl.biketrack.authentication.dto.request.RegisterRequest;
import pl.biketrack.authentication.dto.response.AuthenticationResponse;
import pl.biketrack.authentication.service.AuthenticationService;
import pl.biketrack.exception.dto.response.BaseResponse;
import pl.biketrack.util.MaskingUtil;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public BaseResponse register(@Valid @RequestBody RegisterRequest request) {
        log.info("Start user registration with email: {}", MaskingUtil.maskEmail(request.email()));
        return authenticationService.register(request);
    }

    @PostMapping("/authenticate")
    public AuthenticationResponse authenticate(@Valid @RequestBody LoginRequest request) {
        log.info("Start user authentication with email: {}", MaskingUtil.maskEmail(request.email()));
        return authenticationService.authenticate(request);
    }

    @PostMapping("/refresh-token")
    public AuthenticationResponse refreshToken(HttpServletRequest request) {
        log.info("Start refreshing user token");
        return authenticationService.refreshToken(request);
    }
}