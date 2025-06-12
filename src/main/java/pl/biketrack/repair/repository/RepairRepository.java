package pl.biketrack.repair.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.biketrack.repair.dto.UserRepairStatisticsDto;
import pl.biketrack.repair.dto.response.RepairDetailsResponse;
import pl.biketrack.repair.dto.response.RepairListResponse;
import pl.biketrack.repair.model.Repair;

import java.util.List;
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

    @Query("""
            SELECT new pl.biketrack.repair.dto.UserRepairStatisticsDto(
            r.cost,
            r.currency,
            r.createdDate
            )
            FROM Repair r
            WHERE r.user.uuid = :userUuid
            """)
    List<UserRepairStatisticsDto> getUserRepairStatisticsDto(UUID userUuid);

    @Query("""
            SELECT new pl.biketrack.repair.dto.response.RepairListResponse(
            r.uuid,
            r.title,
            r.repairDate,
            r.createdDate
            )
            FROM Repair r
            WHERE r.user.uuid = :userUuid
            """)
    Page<RepairListResponse> getRepairList(Pageable pageable, UUID userUuid);
}