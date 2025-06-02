package pl.biketrack.authentication.dto.request;

import pl.biketrack.validation.Email;

public record ResetPasswordRequest(

        @Email
        String email
) {}