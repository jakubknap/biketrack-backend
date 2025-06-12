package pl.biketrack.repair.dto.response;

import com.neovisionaries.i18n.CurrencyCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record RepairDetailsResponse(
        UUID repairUuid,
        UUID bikeUuid,
        UUID userUuid,
        String title,
        String description,
        BigDecimal cost,
        CurrencyCode currency,
        LocalDate repairDate,
        LocalDateTime createdDate,
        LocalDateTime lastModifiedDate
) {}