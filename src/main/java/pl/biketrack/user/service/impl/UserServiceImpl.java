package pl.biketrack.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.biketrack.exception.exception.ServiceException;
import pl.biketrack.user.model.User;
import pl.biketrack.user.repository.UserRepository;
import pl.biketrack.user.service.UserService;

import static pl.biketrack.common.enumerated.ResponseCode.E03001;
import static pl.biketrack.util.MaskingUtil.maskEmail;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                             .orElseThrow(() -> {
                                 log.error("User with e-mail: [{}] not found", maskEmail(email));
                                 return new ServiceException(E03001);
                             });
    }
}