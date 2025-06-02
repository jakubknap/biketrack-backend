package pl.biketrack.user.dto.request;

import pl.biketrack.validation.Email;
import pl.biketrack.validation.Nickname;

public record UpdateUserRequest(

        @Nickname
        String nickname,

        @Email
        String email
) {}