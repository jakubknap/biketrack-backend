package pl.biketrack.user.mapper;

import com.neovisionaries.i18n.CurrencyCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.biketrack.authentication.dto.request.RegisterRequest;
import pl.biketrack.repair.dto.RepairStatisticsDto;
import pl.biketrack.user.dto.response.UserDetailsResponse;
import pl.biketrack.user.dto.response.UserStatisticsResponse;
import pl.biketrack.user.enumerated.Role;
import pl.biketrack.user.enumerated.UserStatus;
import pl.biketrack.user.model.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static pl.biketrack.repair.dto.RepairStatisticsDto.getAverageRepairCost;
import static pl.biketrack.repair.dto.RepairStatisticsDto.getLastRepairDate;
import static pl.biketrack.repair.dto.RepairStatisticsDto.getRepairsCurrency;
import static pl.biketrack.repair.dto.RepairStatisticsDto.getTotalRepairCost;
import static pl.biketrack.repair.dto.RepairStatisticsDto.getTotalRepairs;

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

    public static UserStatisticsResponse mapToUserStatisticsResponse(long totalBikes, List<RepairStatisticsDto> repairStatisticsDtoList) {
        long totalRepairs = getTotalRepairs(repairStatisticsDtoList);
        BigDecimal totalRepairCost = getTotalRepairCost(repairStatisticsDtoList);
        BigDecimal averageRepairCost = getAverageRepairCost(totalRepairs, totalRepairCost);
        LocalDateTime lastRepairDate = getLastRepairDate(repairStatisticsDtoList);
        CurrencyCode currency = getRepairsCurrency(repairStatisticsDtoList);

        return new UserStatisticsResponse(totalBikes,
                                          totalRepairs,
                                          totalRepairCost,
                                          averageRepairCost,
                                          currency,
                                          lastRepairDate);
    }
}