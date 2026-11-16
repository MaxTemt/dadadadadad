package com.airport.common.exception;

/**
 * Базовое проверяемое исключение приложения.
 */
public class AirportException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String code;

    public AirportException(String message) {
        this("INTERNAL", message, null);
    }

    public AirportException(String message, Throwable cause) {
        this("INTERNAL", message, cause);
    }

    public AirportException(String code, String message) {
        this(code, message, null);
    }

    public AirportException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + code + "]: " + getMessage();
    }
}
