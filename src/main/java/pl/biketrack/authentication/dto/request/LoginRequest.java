package pl.biketrack.authentication.dto.request;

import pl.biketrack.validation.Email;
import pl.biketrack.validation.Password;

public record LoginRequest(

        @Email
        String email,

        @Password
        String password
) {}