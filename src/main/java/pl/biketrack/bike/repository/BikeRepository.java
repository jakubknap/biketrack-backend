package pl.biketrack.bike.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.biketrack.bike.dto.response.BikeDetailsResponse;
import pl.biketrack.bike.dto.response.BikeListResponse;
import pl.biketrack.bike.dto.response.BikeSelectListResponse;
import pl.biketrack.bike.model.Bike;
import pl.biketrack.dashboard.dto.RecentlyAddedBikeDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BikeRepository extends JpaRepository<Bike, Long> {

    @Query("""
            SELECT b
            FROM Bike b
                     JOIN FETCH b.user
            WHERE b.uuid = :bikeUuid
            """)
    Optional<Bike> findBikeWithUserByUuid(UUID bikeUuid);

    @Query("""
            SELECT b
            FROM Bike b
                     JOIN FETCH b.user
                     JOIN FETCH b.repairs
            WHERE b.uuid = :bikeUuid
            """)
    Optional<Bike> findBikeWithUserAndRepairsByUuid(UUID bikeUuid);

    @Query("""
            SELECT new pl.biketrack.bike.dto.response.BikeDetailsResponse(
            b.uuid,
            b.name,
            b.brand,
            b.model,
            b.type,
            b.purchaseDate,
            b.serialNumber,
            b.mileageKm,
            b.description,
            b.photoUuid,
            b.user.uuid,
            b.createdDate,
            b.lastModifiedDate
            )
            FROM Bike b
            WHERE b.uuid = :bikeUuid
            """)
    Optional<BikeDetailsResponse> getBikeDetails(UUID bikeUuid);

    @Query("""
            SELECT new pl.biketrack.bike.dto.response.BikeSelectListResponse(b.uuid, b.name)
            FROM Bike b
            WHERE b.user.uuid = :userUuid
            """)
    List<BikeSelectListResponse> getBikeList(UUID userUuid);

    @Query("""
            SELECT COUNT(b)
            FROM Bike b
            WHERE b.user.uuid = :userUuid
            """)
    long countByUserUuid(UUID userUuid);

    @Query("""
            SELECT new pl.biketrack.bike.dto.response.BikeListResponse(
            b.uuid,
            b.name,
            b.photoUuid
            )
            FROM Bike b
            WHERE b.user.uuid = :userUuid
            """)
    Page<BikeListResponse> getBikeList(Pageable pageable, UUID userUuid);

    @Query("""
            SELECT NEW pl.biketrack.dashboard.dto.RecentlyAddedBikeDto(b.name, b.uuid)
            FROM Bike b
            WHERE b.user.uuid = :userUuid
            ORDER BY b.createdDate DESC
            """)
    List<RecentlyAddedBikeDto> findRecentlyAddedBikeByUserUuid(UUID userUuid);
}