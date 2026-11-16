package com.airport.common.net;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Запрос клиента. Передаётся по TCP в сериализованном виде.
 */
public class Request implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String command;
    private final Map<String, Object> parameters = new LinkedHashMap<>();

    public Request(String command) {
        if (command == null || command.isBlank()) {
            throw new IllegalArgumentException("Команда не может быть пустой");
        }
        this.command = command;
    }

    public static Request of(String command) {
        return new Request(command);
    }

    public Request with(String key, Object value) {
        parameters.put(key, value);
        return this;
    }

    public String getCommand() {
        return command;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public Object get(String key) {
        return parameters.get(key);
    }

    public boolean has(String key) {
        return parameters.containsKey(key) && parameters.get(key) != null;
    }

    public String getString(String key) {
        Object value = parameters.get(key);
        return value == null ? null : value.toString();
    }

    public String getString(String key, String defaultValue) {
        String value = getString(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    public Long getLong(String key) {
        Object value = parameters.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            return null;
        }
        return Long.valueOf(text);
    }

    public long getLong(String key, long defaultValue) {
        Long value = getLong(key);
        return value == null ? defaultValue : value;
    }

    public Integer getInteger(String key) {
        Object value = parameters.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            return null;
        }
        return Integer.valueOf(text);
    }

    public int getInteger(String key, int defaultValue) {
        Integer value = getInteger(key);
        return value == null ? defaultValue : value;
    }

    public LocalDate getDate(String key) {
        String text = getString(key);
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Некорректная дата: " + text);
        }
    }

    @Override
    public String toString() {
        return "Request{" + command + ", params=" + parameters.keySet() + '}';
    }
}
