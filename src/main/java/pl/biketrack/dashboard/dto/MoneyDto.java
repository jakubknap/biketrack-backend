package pl.biketrack.dashboard.dto;

import com.neovisionaries.i18n.CurrencyCode;

import java.math.BigDecimal;

public record MoneyDto(BigDecimal amount, CurrencyCode currency) {}