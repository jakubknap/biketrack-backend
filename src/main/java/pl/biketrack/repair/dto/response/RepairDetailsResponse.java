package pl.biketrack.repair.dto.response;

import com.neovisionaries.i18n.CurrencyCode;
import pl.biketrack.bike.dto.response.BikeSelectListResponse;
import pl.biketrack.dashboard.dto.MoneyDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record RepairDetailsResponse(UUID repairUuid,
                                    BikeSelectListResponse bike,
                                    UUID userUuid,
                                    String title,
                                    String description,
                                    MoneyDto cost,
                                    LocalDate repairDate,
                                    LocalDateTime createdDate,
                                    LocalDateTime lastModifiedDate) {

    public RepairDetailsResponse(UUID repairUuid,
                                 UUID bikeUuid,
                                 String bikeName,
                                 UUID userUuid,
                                 String title,
                                 String description,
                                 BigDecimal cost,
                                 CurrencyCode currency,
                                 LocalDate repairDate,
                                 LocalDateTime createdDate,
                                 LocalDateTime lastModifiedDate) {
        this(repairUuid,
             new BikeSelectListResponse(bikeUuid, bikeName),
             userUuid,
             title,
             description,
             new MoneyDto(cost, currency),
             repairDate,
             createdDate,
             lastModifiedDate);
    }
}