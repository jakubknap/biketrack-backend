package pl.biketrack.user.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.biketrack.authentication.dto.request.RegisterRequest;
import pl.biketrack.user.dto.response.UserDetailsResponse;
import pl.biketrack.user.enumerated.Role;
import pl.biketrack.user.enumerated.UserStatus;
import pl.biketrack.user.model.User;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    public User mapToUserEntity(RegisterRequest request) {
        return User.builder()
                   .uuid(UUID.randomUUID())
                   .nickname(request.nickname())
                   .email(request.email())
                   .password(passwordEncoder.encode(request.password()))
                   .status(UserStatus.REGISTERED)
                   .role(Role.USER)
                   .build();
    }

    public static UserDetailsResponse buildUserDetailsResponse(User user) {
        return new UserDetailsResponse(user.getNickname(), user.getEmail());
    }
}