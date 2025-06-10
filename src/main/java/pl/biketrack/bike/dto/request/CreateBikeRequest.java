package pl.biketrack.bike.dto.request;

import jakarta.validation.constraints.NotBlank;
import pl.biketrack.validation.SafeText;

import java.time.LocalDate;

public record CreateBikeRequest(

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
        String description
) {}