package pl.biketrack.repair.dto.request;

import com.neovisionaries.i18n.CurrencyCode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.biketrack.validation.CostCurrencyConsistency;
import pl.biketrack.validation.SafeText;
import pl.biketrack.validation.SupportedCurrencies;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static com.neovisionaries.i18n.CurrencyCode.PLN;

@CostCurrencyConsistency
public record AddRepairRequest(

        @NotNull
        UUID bikeUuid,

        @NotBlank
        @SafeText
        @Size(max = 255)
        String title,

        @SafeText
        @Size(max = 255)
        String description,

        @Min(0)
        BigDecimal cost,

        @SupportedCurrencies(supportedCurrencies = {PLN})
        CurrencyCode currency,

        LocalDate repairDate
) {}