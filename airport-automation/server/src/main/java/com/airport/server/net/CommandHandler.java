package com.airport.server.net;

import com.airport.common.net.Request;
import com.airport.common.net.Response;

/**
 * Команда сервера (паттерн «Команда»).
 */
@FunctionalInterface
public interface CommandHandler {
    Response handle(Request request, SessionContext context) throws Exception;
}
