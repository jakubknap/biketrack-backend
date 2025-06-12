package pl.biketrack.user.dto.response;

import pl.biketrack.user.enumerated.UserStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDetailsResponse(
        UUID uuid,
        String nickname,
        String email,
        UserStatus status,
        LocalDateTime createdDate
) {}