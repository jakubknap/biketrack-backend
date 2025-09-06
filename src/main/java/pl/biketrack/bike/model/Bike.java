package pl.biketrack.bike.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import pl.biketrack.common.entity.auditable.FullAuditEntity;
import pl.biketrack.repair.model.Repair;
import pl.biketrack.user.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static jakarta.persistence.CascadeType.REMOVE;
import static jakarta.persistence.FetchType.LAZY;

@Getter
@Setter
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Bike extends FullAuditEntity {

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Column(nullable = false)
    private String name;

    private String brand;

    private String model;

    @Column(nullable = false)
    private String type;

    private LocalDate purchaseDate;

    private String serialNumber;

    private String mileageKm;

    private String description;

    private UUID photoFileName;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "bike", cascade = REMOVE, orphanRemoval = true)
    private List<Repair> repairs = new ArrayList<>();

    @Transient
    public UUID getUserUuid() {
        return user.getUuid();
    }
}