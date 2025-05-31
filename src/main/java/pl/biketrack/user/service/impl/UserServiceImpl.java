package pl.biketrack.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.biketrack.exception.dto.response.BaseResponse;
import pl.biketrack.exception.exception.ServiceException;
import pl.biketrack.security.util.SecurityUtils;
import pl.biketrack.user.dto.request.ChangePasswordRequest;
import pl.biketrack.user.dto.request.UpdateUserRequest;
import pl.biketrack.user.model.User;
import pl.biketrack.user.repository.UserRepository;
import pl.biketrack.user.service.UserService;

import static pl.biketrack.common.enumerated.ResponseCode.E03001;
import static pl.biketrack.common.enumerated.ResponseCode.E03004;
import static pl.biketrack.common.enumerated.ResponseCode.E03005;
import static pl.biketrack.common.enumerated.ResponseCode.E03006;
import static pl.biketrack.common.enumerated.ResponseCode.E03007;
import static pl.biketrack.common.enumerated.ResponseCode.S00000;
import static pl.biketrack.util.MaskingUtil.maskEmail;
import static pl.biketrack.util.StringUtil.notEquals;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                             .orElseThrow(() -> {
                                 log.error("User with e-mail: [{}] not found", maskEmail(email));
                                 return new ServiceException(E03001);
                             });
    }

    @Override
    public BaseResponse updateUser(UpdateUserRequest request) {
        User user = SecurityUtils.getLoggedUser();
        log.info("Start the process of updating user with UUID: [{}]", user.getUuid());

        if (notEquals(request.email(), user.getEmail())) {
            boolean isEmailTaken = userRepository.isEmailTaken(request.email(), user.getUuid());
            if (isEmailTaken) {
                log.error("User cannot be updated. There is another user with that e-mail.");
                throw new ServiceException(E03006);
            }
        }

        if (notEquals(request.nickname(), user.getNickname())) {
            boolean isNicknameTaken = userRepository.isNicknameTaken(request.nickname(), user.getUuid());
            if (isNicknameTaken) {
                log.error("User cannot be updated. There is another user with that nickname.");
                throw new ServiceException(E03007);
            }
        }

        user.setEmail(request.email());
        user.setNickname(request.nickname());

        userRepository.save(user);

        log.info("Successfully completed process of updating user with UUID: [{}]", user.getUuid());
        return new BaseResponse(S00000);
    }

    @Override
    public BaseResponse changePassword(ChangePasswordRequest request) {
        User user = SecurityUtils.getLoggedUser();
        log.info("Start the process of changing password for user with UUID: [{}]", user.getUuid());

        validatePasswordChangeRequest(request, user);

        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);

        log.info("Successfully completed the password change process for the user with UUID: [{}]", user.getUuid());
        return new BaseResponse(S00000);
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
}