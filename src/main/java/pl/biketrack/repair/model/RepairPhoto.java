package pl.biketrack.repair.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.biketrack.common.entity.auditable.FullAuditEntity;

import java.util.UUID;

import static jakarta.persistence.FetchType.LAZY;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class RepairPhoto extends FullAuditEntity {

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "repair_id")
    private Repair repair;
}