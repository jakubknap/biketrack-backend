package pl.biketrack.security.util;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.biketrack.exception.exception.ServiceException;
import pl.biketrack.user.model.User;

import java.util.UUID;

import static java.util.Objects.isNull;
import static pl.biketrack.common.enumerated.ResponseCode.E01002;
import static pl.biketrack.common.enumerated.ResponseCode.E01003;

@UtilityClass
public class SecurityUtils {

    public static User getLoggedUser() {
        Authentication authentication = getAuthentication();

        if (isNull(authentication) || !authentication.isAuthenticated()) {
            throw new ServiceException(E01002);
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            return (User) principal;
        }

        throw new ServiceException(E01003);
    }

    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext()
                                    .getAuthentication();
    }

    public static UUID getLoggedUserUUID() {
        return getLoggedUser().getUuid();
    }
}