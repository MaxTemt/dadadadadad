package com.airport.common.exception;

/**
 * Ошибка при работе с базой данных.
 */
public class DataAccessException extends AirportException {

    private static final long serialVersionUID = 1L;

    public DataAccessException(String message, Throwable cause) {
        super("DATA_ACCESS", message, cause);
    }

    public DataAccessException(String message) {
        super("DATA_ACCESS", message);
    }
}
