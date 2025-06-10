package pl.biketrack.bike.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.biketrack.bike.dto.response.BikeDetailsResponse;
import pl.biketrack.bike.model.Bike;

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
            b.user.uuid
            )
            FROM Bike b
                JOIN b.user
            WHERE b.uuid = :bikeUuid
            """)
    Optional<BikeDetailsResponse> getBikeDetails(UUID bikeUuid);
}