package pl.biketrack.token.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.biketrack.common.entity.auditable.DateAuditEntity;
import pl.biketrack.token.enumerated.TokenType;
import pl.biketrack.user.model.User;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Token extends DateAuditEntity {

    @Enumerated(STRING)
    @Column(nullable = false)
    private TokenType tokenType;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private boolean revoked;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;
}