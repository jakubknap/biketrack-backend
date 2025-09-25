package pl.biketrack.repair.dto.request;

import com.neovisionaries.i18n.CurrencyCode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pl.biketrack.validation.CostCurrencyConsistency;
import pl.biketrack.validation.SafeText;
import pl.biketrack.validation.SupportedCurrencies;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.neovisionaries.i18n.CurrencyCode.PLN;

@CostCurrencyConsistency
public record UpdateRepairRequest(

        @NotNull
        UUID repairUuid,

        @NotBlank
        @SafeText
        String title,

        @SafeText
        String description,

        @Min(0)
        BigDecimal cost,

        @SupportedCurrencies(supportedCurrencies = {PLN})
        CurrencyCode currency,

        LocalDate repairDate,

        List<UUID> deletedPhotoUuids
) {}