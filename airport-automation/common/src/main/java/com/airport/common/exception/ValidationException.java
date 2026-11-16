package com.airport.common.exception;

/**
 * Ошибка валидации данных.
 */
public class ValidationException extends AirportException {

    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super("VALIDATION", message);
    }

    public ValidationException(String message, Throwable cause) {
        super("VALIDATION", message, cause);
    }
}
