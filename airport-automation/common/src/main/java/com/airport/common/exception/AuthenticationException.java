package com.airport.common.exception;

/**
 * Ошибка аутентификации.
 */
public class AuthenticationException extends AirportException {

    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super("AUTHENTICATION", message);
    }
}
