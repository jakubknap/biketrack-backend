package pl.biketrack.bike.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import pl.biketrack.validation.SafeText;

import java.time.LocalDate;

public record CreateBikeRequest(

        @NotBlank
        @SafeText
        @Size(max = 255)
        String name,

        @SafeText
        @Size(max = 255)
        String brand,

        @SafeText
        @Size(max = 255)
        String model,

        @NotBlank
        @SafeText
        @Size(max = 255)
        String type,

        LocalDate purchaseDate,

        @SafeText
        @Size(max = 255)
        String serialNumber,

        @SafeText
        @Size(max = 255)
        String mileageKm,

        @SafeText
        @Size(max = 255)
        String description
) {}