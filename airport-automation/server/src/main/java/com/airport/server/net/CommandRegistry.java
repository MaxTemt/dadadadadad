package com.airport.server.net;

import com.airport.common.exception.AirportException;
import com.airport.common.exception.ValidationException;
import com.airport.common.net.Command;
import com.airport.common.net.Request;
import com.airport.common.net.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Реестр команд и централизованная обработка исключительных ситуаций.
 */
public final class CommandRegistry {

    private static final Logger LOGGER = Logger.getLogger(CommandRegistry.class.getName());
    private static final Set<String> PUBLIC_COMMANDS = Set.of(Command.PING, Command.LOGIN);

    private final Map<String, CommandHandler> handlers = new HashMap<>();

    public void register(String command, CommandHandler handler) {
        if (handlers.put(command, handler) != null) {
            throw new IllegalStateException("Команда уже зарегистрирована: " + command);
        }
    }

    public Response dispatch(Request request, SessionContext context) {
        String command = request.getCommand();
        try {
            if (!PUBLIC_COMMANDS.contains(command)) {
                context.requireAuthenticated();
            }
            CommandHandler handler = handlers.get(command);
            if (handler == null) {
                throw new ValidationException("Неизвестная команда: " + command);
            }
            return handler.handle(request, context);
        } catch (AirportException exception) {
            if (!(exception instanceof ValidationException)) {
                LOGGER.log(Level.INFO, "Команда {0}: {1}", new Object[]{command, exception.getMessage()});
            }
            return Response.fail(exception.getCode(), exception.getMessage());
        } catch (IllegalArgumentException exception) {
            LOGGER.log(Level.INFO, "Команда " + command + ": " + exception.getMessage());
            return Response.fail("VALIDATION", exception.getMessage());
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Ошибка выполнения команды " + command, exception);
            return Response.fail("INTERNAL", "Внутренняя ошибка сервера. Попробуйте позже.");
        }
    }
}
