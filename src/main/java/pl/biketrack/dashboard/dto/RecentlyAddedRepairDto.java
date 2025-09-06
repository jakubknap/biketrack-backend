package pl.biketrack.dashboard.dto;

import com.neovisionaries.i18n.CurrencyCode;

import java.math.BigDecimal;

public record RecentlyAddedRepairDto(String title, MoneyDto repairCost) {

    public RecentlyAddedRepairDto(String title, BigDecimal amount, CurrencyCode currency) {
        this(title, new MoneyDto(amount, currency));
    }
}