package pl.biketrack.user.mapper;

import com.neovisionaries.i18n.CurrencyCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.biketrack.authentication.dto.request.RegisterRequest;
import pl.biketrack.repair.dto.UserRepairStatisticsDto;
import pl.biketrack.user.dto.response.UserDetailsResponse;
import pl.biketrack.user.dto.response.UserStatisticsResponse;
import pl.biketrack.user.enumerated.Role;
import pl.biketrack.user.enumerated.UserStatus;
import pl.biketrack.user.model.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static pl.biketrack.repair.dto.UserRepairStatisticsDto.getAverageRepairCost;
import static pl.biketrack.repair.dto.UserRepairStatisticsDto.getLastRepairDate;
import static pl.biketrack.repair.dto.UserRepairStatisticsDto.getRepairsCurrency;
import static pl.biketrack.repair.dto.UserRepairStatisticsDto.getTotalRepairCost;
import static pl.biketrack.repair.dto.UserRepairStatisticsDto.getTotalRepairs;

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
        return new UserDetailsResponse(user.getUuid(),
                                       user.getNickname(),
                                       user.getEmail(),
                                       user.getStatus(),
                                       user.getCreatedDate());
    }

    public static UserStatisticsResponse mapToUserStatisticsResponse(long totalBikes, List<UserRepairStatisticsDto> userRepairStatisticsDto) {
        long totalRepairs = getTotalRepairs(userRepairStatisticsDto);
        BigDecimal totalRepairCost = getTotalRepairCost(userRepairStatisticsDto);
        BigDecimal averageRepairCost = getAverageRepairCost(totalRepairs, totalRepairCost);
        LocalDateTime lastRepairDate = getLastRepairDate(userRepairStatisticsDto);
        CurrencyCode currency = getRepairsCurrency(userRepairStatisticsDto);

        return new UserStatisticsResponse(totalBikes,
                                          totalRepairs,
                                          totalRepairCost,
                                          averageRepairCost,
                                          currency,
                                          lastRepairDate);
    }
}