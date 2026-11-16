package com.airport.common.exception;

/**
 * Недостаточно прав для выполнения операции.
 */
public class AuthorizationException extends AirportException {

    private static final long serialVersionUID = 1L;

    public AuthorizationException(String message) {
        super("AUTHORIZATION", message);
    }
}
