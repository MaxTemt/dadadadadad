package com.airport.common.model;

/**
 * Статус рейса.
 */
public enum FlightStatus {

    SCHEDULED("По расписанию"),
    BOARDING("Посадка"),
    DEPARTED("Вылетел"),
    DELAYED("Задержан"),
    CANCELLED("Отменён");

    private final String title;

    FlightStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public static FlightStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            return SCHEDULED;
        }
        for (FlightStatus status : values()) {
            if (status.name().equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("Неизвестный статус рейса: " + value);
    }

    @Override
    public String toString() {
        return title;
    }
}
