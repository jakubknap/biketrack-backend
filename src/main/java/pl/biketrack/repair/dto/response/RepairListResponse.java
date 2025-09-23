package pl.biketrack.repair.dto.response;

import com.neovisionaries.i18n.CurrencyCode;
import pl.biketrack.dashboard.dto.MoneyDto;
import pl.biketrack.repair.dto.RepairBikeDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record RepairListResponse(UUID uuid,
                                 String title,
                                 LocalDateTime createdDate,
                                 MoneyDto cost,
                                 RepairBikeDto bike) {

    public RepairListResponse(UUID uuid, String title, LocalDateTime createdDate, BigDecimal cost, CurrencyCode currency, UUID bikeUuid, String bikeName) {
        this(uuid, title, createdDate, new MoneyDto(cost, currency), new RepairBikeDto(bikeUuid, bikeName));
    }
}