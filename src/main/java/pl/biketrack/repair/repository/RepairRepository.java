package pl.biketrack.repair.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.biketrack.bike.dto.response.BikeRepairResponse;
import pl.biketrack.dashboard.dto.RecentlyAddedRepairDto;
import pl.biketrack.repair.dto.RepairStatisticsDto;
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
            r.bike.name,
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
            SELECT new pl.biketrack.repair.dto.RepairStatisticsDto(
            r.cost,
            r.currency,
            r.createdDate
            )
            FROM Repair r
            WHERE r.user.uuid = :userUuid
            """)
    List<RepairStatisticsDto> getRepairStatisticsDtoForUser(UUID userUuid);

    @Query("""
            SELECT new pl.biketrack.repair.dto.RepairStatisticsDto(
            r.cost,
            r.currency,
            r.createdDate
            )
            FROM Repair r
            WHERE r.bike.uuid = :bikeUuid
            """)
    List<RepairStatisticsDto> getRepairStatisticsDtoForBike(UUID bikeUuid);

    @Query("""
            SELECT new pl.biketrack.repair.dto.response.RepairListResponse(
            r.uuid,
            r.title,
            r.createdDate,
            r.cost,
            r.currency,
            b.uuid,
            b.name
            )
            FROM Repair r
            JOIN r.bike b
            WHERE r.user.uuid = :userUuid
            """)
    Page<RepairListResponse> getRepairList(Pageable pageable, UUID userUuid);

    @Query("""
            SELECT new pl.biketrack.bike.dto.response.BikeRepairResponse(
            r.uuid,
            r.title,
            r.cost,
            r.currency,
            r.createdDate
            )
            FROM Repair r
            WHERE r.bike.uuid = :bikeUuid
            """)
    Page<BikeRepairResponse> getRepairsByBike(Pageable pageable, UUID bikeUuid);

    @Query("""
            SELECT new pl.biketrack.dashboard.dto.RecentlyAddedRepairDto(r.uuid, r.title, r.cost, r.currency)
            FROM Repair r
            WHERE r.user.uuid = :userUuid
            ORDER BY r.createdDate DESC
            """)
    List<RecentlyAddedRepairDto> findRecentlyAddedRepairByUserUuid(UUID userUuid);
}