package pl.biketrack.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.biketrack.bike.repository.BikeRepository;
import pl.biketrack.exception.dto.response.BaseResponse;
import pl.biketrack.exception.exception.ServiceException;
import pl.biketrack.repair.dto.RepairStatisticsDto;
import pl.biketrack.repair.repository.RepairRepository;
import pl.biketrack.security.util.SecurityUtils;
import pl.biketrack.token.repository.TokenRepository;
import pl.biketrack.user.dto.request.ChangePasswordRequest;
import pl.biketrack.user.dto.request.UpdateUserRequest;
import pl.biketrack.user.dto.response.UserDetailsResponse;
import pl.biketrack.user.dto.response.UserStatisticsResponse;
import pl.biketrack.user.model.User;
import pl.biketrack.user.repository.UserRepository;
import pl.biketrack.user.service.UserService;

import java.util.List;
import java.util.UUID;

import static pl.biketrack.common.enumerated.ResponseCode.E03001;
import static pl.biketrack.common.enumerated.ResponseCode.E03004;
import static pl.biketrack.common.enumerated.ResponseCode.E03005;
import static pl.biketrack.common.enumerated.ResponseCode.E03006;
import static pl.biketrack.common.enumerated.ResponseCode.E03007;
import static pl.biketrack.common.enumerated.ResponseCode.S00000;
import static pl.biketrack.user.enumerated.UserStatus.DEACTIVATED;
import static pl.biketrack.user.mapper.UserMapper.buildUserDetailsResponse;
import static pl.biketrack.user.mapper.UserMapper.mapToUserStatisticsResponse;
import static pl.biketrack.util.MaskingUtil.maskEmail;
import static pl.biketrack.util.StringUtil.notEquals;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;
    private final BikeRepository bikeRepository;
    private final RepairRepository repairRepository;

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                             .orElseThrow(() -> {
                                 log.error("User with e-mail: [{}] not found", maskEmail(email));
                                 return new ServiceException(E03001);
                             });
    }

    @Override
    public UserDetailsResponse getUserDetails() {
        User user = SecurityUtils.getLoggedUser();
        log.info("Start the process of retrieving a user details for user with UUID: [{}]", user.getUuid());
        return buildUserDetailsResponse(user);
    }

    @Override
    public BaseResponse updateUser(UpdateUserRequest request) {
        User user = SecurityUtils.getLoggedUser();
        UUID userUuid = user.getUuid();

        log.info("Start the process of updating user with UUID: [{}]", userUuid);

        validateDataAvailability(request, user);
        updateUser(request, user);

        log.info("Successfully completed process of updating user with UUID: [{}]", userUuid);
        return new BaseResponse(S00000);
    }

    @Override
    public BaseResponse changePassword(ChangePasswordRequest request) {
        User user = SecurityUtils.getLoggedUser();
        UUID userUuid = user.getUuid();
        log.info("Start the process of changing password for user with UUID: [{}]", userUuid);

        validatePasswordChangeRequest(request, user);
        updatePassword(request, user);

        log.info("Successfully completed the password change process for the user with UUID: [{}]", userUuid);
        return new BaseResponse(S00000);
    }

    @Override
    public UserStatisticsResponse getUserStatistics() {
        UUID userUuid = SecurityUtils.getLoggedUserUUID();
        log.info("Start the process of getting user statistics for user with UUID: [{}]", userUuid);

        long totalBikes = bikeRepository.countByUserUuid(userUuid);
        List<RepairStatisticsDto> repairStatisticsDto = repairRepository.getRepairStatisticsDtoForUser(userUuid);

        return mapToUserStatisticsResponse(totalBikes, repairStatisticsDto);
    }

    @Override
    @Transactional
    public BaseResponse deleteUser() {
        User user = SecurityUtils.getLoggedUser();
        UUID userUuid = user.getUuid();
        log.info("Start the process of deleting user with UUID: [{}]", userUuid);

        user.setStatus(DEACTIVATED);
        userRepository.save(user);
        tokenRepository.revokeAllValidTokensByUserUuid(userUuid);

        log.info("Successfully completed deleting user with UUID: [{}]", userUuid);
        return new BaseResponse(S00000);
    }

    private void validateDataAvailability(UpdateUserRequest request, User user) {
        UUID userUuid = user.getUuid();

        boolean isEmailChanged = notEquals(request.email(), user.getEmail());
        if (isEmailChanged) {
            boolean isEmailTaken = userRepository.isEmailTaken(request.email(), userUuid);
            if (isEmailTaken) {
                log.error("User cannot be updated. There is another user with that e-mail.");
                throw new ServiceException(E03006);
            }
        }

        boolean isNicknameChanged = notEquals(request.nickname(), user.getNickname());
        if (isNicknameChanged) {
            boolean isNicknameTaken = userRepository.isNicknameTaken(request.nickname(), userUuid);
            if (isNicknameTaken) {
                log.error("User cannot be updated. There is another user with that nickname.");
                throw new ServiceException(E03007);
            }
        }
    }

    private void updateUser(UpdateUserRequest request, User user) {
        user.setEmail(request.email());
        user.setNickname(request.nickname());

        userRepository.save(user);
    }

    private void validatePasswordChangeRequest(ChangePasswordRequest request, User user) {
        if (notEquals(request.password(), request.passwordRepeat())) {
            log.error("Password does not match password repeated");
            throw new ServiceException(E03004);
        }

        if (passwordEncoder.matches(request.password(), user.getPassword())) {
            log.error("The password is the same as the user's current password");
            throw new ServiceException(E03005);
        }
    }

    private void updatePassword(ChangePasswordRequest request, User user) {
        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);
    }
}