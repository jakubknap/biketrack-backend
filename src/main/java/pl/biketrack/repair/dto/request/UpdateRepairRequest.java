package pl.biketrack.repair.dto.request;

import com.neovisionaries.i18n.CurrencyCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pl.biketrack.validation.SafeText;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateRepairRequest(

        @NotNull
        UUID repairUuid,

        @NotBlank
        @SafeText
        String title,

        @SafeText
        String description,

        BigDecimal cost,

        CurrencyCode currency,

        LocalDate repairDate
) {}