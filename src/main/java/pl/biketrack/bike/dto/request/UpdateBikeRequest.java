package pl.biketrack.bike.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pl.biketrack.validation.SafeText;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateBikeRequest(

        @NotNull
        UUID bikeUuid,

        @NotBlank
        @SafeText
        String name,

        @SafeText
        String brand,

        @SafeText
        String model,

        @NotBlank
        @SafeText
        String type,

        LocalDate purchaseDate,

        @SafeText
        String serialNumber,

        @SafeText
        String mileageKm,

        @SafeText
        String description,

        boolean deletePhoto
) {}