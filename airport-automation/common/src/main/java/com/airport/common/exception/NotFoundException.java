package com.airport.common.exception;

/**
 * Запрошенная запись не найдена.
 */
public class NotFoundException extends AirportException {

    private static final long serialVersionUID = 1L;

    public NotFoundException(String message) {
        super("NOT_FOUND", message);
    }
}
