package pl.biketrack.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.biketrack.common.entity.auditable.DateAuditEntity;
import pl.biketrack.user.enumerated.Role;
import pl.biketrack.user.enumerated.UserStatus;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static jakarta.persistence.EnumType.STRING;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_user")
public class User extends DateAuditEntity implements UserDetails {

    private static final String ROLE_PREFIX = "ROLE_";

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Enumerated(STRING)
    @Column(nullable = false)
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(ROLE_PREFIX + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !status.isBlocked();
    }

    @Override
    public boolean isEnabled() {
        return status.isActive();
    }
}