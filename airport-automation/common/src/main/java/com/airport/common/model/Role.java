package com.airport.common.model;

/**
 * Роль пользователя системы.
 */
public enum Role {

    ADMIN("Администратор"),
    USER("Пользователь");

    private final String title;

    Role(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public static Role fromString(String value) {
        if (value == null) {
            return USER;
        }
        for (Role role : values()) {
            if (role.name().equalsIgnoreCase(value.trim())) {
                return role;
            }
        }
        throw new IllegalArgumentException("Неизвестная роль: " + value);
    }

    @Override
    public String toString() {
        return title;
    }
}
