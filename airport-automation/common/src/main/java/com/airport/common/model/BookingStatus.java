package com.airport.common.model;

/**
 * Статус бронирования.
 */
public enum BookingStatus {

    CONFIRMED("Подтверждено"),
    CHECKED_IN("Регистрация пройдена"),
    CANCELLED("Отменено");

    private final String title;

    BookingStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public static BookingStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            return CONFIRMED;
        }
        for (BookingStatus status : values()) {
            if (status.name().equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("Неизвестный статус бронирования: " + value);
    }

    @Override
    public String toString() {
        return title;
    }
}
