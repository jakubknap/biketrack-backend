package pl.biketrack.repair.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.biketrack.bike.dto.response.BikeRepairResponse;
import pl.biketrack.dashboard.dto.RecentlyAddedRepairDto;
import pl.biketrack.repair.dto.RepairStatisticsDto;
import pl.biketrack.repair.dto.response.RepairListResponse;
import pl.biketrack.repair.model.Repair;
import pl.biketrack.statistics.dto.RepairsPerMonthDtoProjection;
import pl.biketrack.statistics.dto.response.StatisticsResponse.AverageRepairCostPerBikeProjection;
import pl.biketrack.statistics.dto.response.StatisticsResponse.RepairsPerBikeProjection;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepairRepository extends JpaRepository<Repair, Long> {

    @Query("""
            SELECT r
            FROM Repair r
                     JOIN FETCH r.user
            WHERE r.uuid = :repairUuid
            """)
    Optional<Repair> findRepairWithUserByUuid(UUID repairUuid);

    @Query("""
            SELECT r
            FROM Repair r
                     JOIN FETCH r.user
                     JOIN FETCH r.bike
            WHERE r.uuid = :repairUuid
            """)
    Optional<Repair> findRepairWithUserAndBikeByUuid(UUID repairUuid);

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

    @Query(value = """
            SELECT b.name   as bikeName,
                   count(r) as repairs
            FROM repair r
                     INNER JOIN public.bike b on b.id = r.bike_id
                     INNER JOIN public._user u on u.id = b.user_id
            WHERE u.uuid = :userUuid
            GROUP BY b.name
            ORDER BY COUNT(r) DESC
            """, nativeQuery = true)
    List<RepairsPerBikeProjection> findRepairsPerBikeByUserUuid(UUID userUuid);

    @Query(value = """
            SELECT b.name      as bikeName,
                   AVG(r.cost) as averageCost
            FROM repair r
                     INNER JOIN public.bike b on b.id = r.bike_id
                     INNER JOIN public._user u on u.id = b.user_id
            WHERE u.uuid = :userUuid
              AND r.cost IS NOT NULL
            GROUP BY b.name
            ORDER BY AVG(r.cost) DESC
            """, nativeQuery = true)
    List<AverageRepairCostPerBikeProjection> findAverageRepairCostPerBikeByUserUuid(UUID userUuid);

    @Query(value = """
            SELECT EXTRACT(MONTH FROM r.repair_date) as monthNumber,
                   COUNT(r)                          as repairsCount
            FROM repair r
                     INNER JOIN public.bike b on b.id = r.bike_id
                     INNER JOIN public._user u on u.id = b.user_id
            WHERE u.uuid = :userUuid
              AND EXTRACT(YEAR FROM r.repair_date) = :year
            GROUP BY EXTRACT(MONTH FROM r.repair_date)
            ORDER BY EXTRACT(MONTH FROM r.repair_date)
            """, nativeQuery = true)
    List<RepairsPerMonthDtoProjection> countRepairsPerMonthForYearByUserUuidAndYear(UUID userUuid, int year);
}