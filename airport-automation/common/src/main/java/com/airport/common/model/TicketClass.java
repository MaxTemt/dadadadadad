package com.airport.common.model;

import java.math.BigDecimal;

/**
 * Класс обслуживания и коэффициент стоимости билета.
 */
public enum TicketClass {

    ECONOMY("Эконом", new BigDecimal("1.00")),
    BUSINESS("Бизнес", new BigDecimal("1.80")),
    FIRST("Первый", new BigDecimal("2.70"));

    private final String title;
    private final BigDecimal priceMultiplier;

    TicketClass(String title, BigDecimal priceMultiplier) {
        this.title = title;
        this.priceMultiplier = priceMultiplier;
    }

    public String getTitle() {
        return title;
    }

    public BigDecimal getPriceMultiplier() {
        return priceMultiplier;
    }

    public static TicketClass fromString(String value) {
        if (value == null || value.isBlank()) {
            return ECONOMY;
        }
        for (TicketClass ticketClass : values()) {
            if (ticketClass.name().equalsIgnoreCase(value.trim())) {
                return ticketClass;
            }
        }
        throw new IllegalArgumentException("Неизвестный класс обслуживания: " + value);
    }

    @Override
    public String toString() {
        return title;
    }
}
