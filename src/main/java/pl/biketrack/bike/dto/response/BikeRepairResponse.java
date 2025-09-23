package pl.biketrack.bike.dto.response;

import com.neovisionaries.i18n.CurrencyCode;
import pl.biketrack.dashboard.dto.MoneyDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BikeRepairResponse(UUID repairUuid,
                                 String title,
                                 MoneyDto cost,
                                 LocalDateTime createdDate) {

    public BikeRepairResponse(UUID repairUuid,
                              String title,
                              BigDecimal cost,
                              CurrencyCode currency,
                              LocalDateTime createdDate) {
        this(repairUuid, title, new MoneyDto(cost, currency), createdDate);
    }
}