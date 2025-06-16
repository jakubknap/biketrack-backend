package pl.biketrack.repair.model;

import com.neovisionaries.i18n.CurrencyCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import pl.biketrack.bike.model.Bike;
import pl.biketrack.common.entity.auditable.FullAuditEntity;
import pl.biketrack.user.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;

@Getter
@Setter
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Repair extends FullAuditEntity {

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Column(nullable = false)
    private String title;

    private String description;

    private BigDecimal cost;

    @Enumerated(STRING)
    private CurrencyCode currency;

    private LocalDate repairDate;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "bike_id", nullable = false, updatable = false)
    private Bike bike;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Transient
    public UUID getUserUuid() {
        return user.getUuid();
    }
}