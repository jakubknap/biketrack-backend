package pl.biketrack.bike.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
        UUID photo,
        LocalDateTime createdDate,
        LocalDateTime lastModifiedDate,

        @JsonIgnore
        UUID userUuid
) {}