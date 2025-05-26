package pl.biketrack.authentication.service;

import jakarta.servlet.http.HttpServletRequest;
import pl.biketrack.authentication.dto.request.LoginRequest;
import pl.biketrack.authentication.dto.request.RegisterRequest;
import pl.biketrack.authentication.dto.response.AuthenticationResponse;
import pl.biketrack.exception.dto.response.BaseResponse;

import java.util.UUID;

public interface AuthenticationService {

    BaseResponse register(RegisterRequest request);

    BaseResponse activateAccount(UUID token);

    AuthenticationResponse authenticate(LoginRequest request);

    AuthenticationResponse refreshToken(HttpServletRequest request);
}