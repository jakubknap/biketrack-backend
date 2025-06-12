package pl.biketrack.repair.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.biketrack.repair.dto.response.RepairDetailsResponse;
import pl.biketrack.repair.model.Repair;

import java.util.Optional;
import java.util.UUID;

public interface RepairRepository extends JpaRepository<Repair, Long> {

    @Query("""
            SELECT new pl.biketrack.repair.dto.response.RepairDetailsResponse(
            r.uuid,
            r.bike.uuid,
            r.user.uuid,
            r.title,
            r.description,
            r.cost,
            r.currency,
            r.repairDate,
            r.createdDate,
            r.lastModifiedDate
            )
            FROM Repair r
                JOIN r.user
                JOIN r.bike
            WHERE r.uuid = :repairUuid
            """)
    Optional<RepairDetailsResponse> getRepairDetails(UUID repairUuid);

    @Query("""
            SELECT r
            FROM Repair r
                     JOIN FETCH r.user
            WHERE r.uuid = :repairUuid
            """)
    Optional<Repair> findRepairWithUserByUuid(UUID repairUuid);
}