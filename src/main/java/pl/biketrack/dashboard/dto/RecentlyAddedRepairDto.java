package pl.biketrack.dashboard.dto;

import com.neovisionaries.i18n.CurrencyCode;

import java.math.BigDecimal;
import java.util.UUID;

public record RecentlyAddedRepairDto(UUID uuid, String title, MoneyDto repairCost) {

    public RecentlyAddedRepairDto(UUID uuid, String title, BigDecimal amount, CurrencyCode currency) {
        this(uuid, title, new MoneyDto(amount, currency));
    }
}