package pl.biketrack.bike.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record BikeDetailsResponse(
        UUID bikeUuid,
        String name,
        String brand,
        String model,
        String type,
        LocalDate purchaseDate,
        String serialNumber,
        String mileageKm,
        String description,
        UUID photoFileName,
        UUID userUuid,
        LocalDateTime createdDate,
        LocalDateTime lastModifiedDate
) {}