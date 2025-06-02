package pl.biketrack.authentication.dto.request;

import pl.biketrack.validation.Email;
import pl.biketrack.validation.Nickname;
import pl.biketrack.validation.Password;

public record RegisterRequest(

        @Nickname
        String nickname,

        @Email
        String email,

        @Password
        String password
) {}