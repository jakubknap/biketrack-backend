package pl.biketrack.auditing;

import org.springframework.data.domain.AuditorAware;
import pl.biketrack.user.model.User;

import java.util.Optional;

import static pl.biketrack.security.util.SecurityUtils.getLoggedUser;

public class ApplicationAuditAware implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        User user = getLoggedUser();
        return Optional.ofNullable(user.getId());
    }
}