package com.airport.server.net;

import com.airport.common.net.Command;
import com.airport.common.net.Request;
import com.airport.common.net.Response;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Обработчик одного клиентского подключения. Выполняется в отдельном потоке пула,
 * обслуживает последовательность запросов до разрыва соединения.
 */
public final class ClientSession implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ClientSession.class.getName());

    private final Socket socket;
    private final CommandRegistry registry;
    private final SessionContext context;

    public ClientSession(Socket socket, CommandRegistry registry) {
        this.socket = socket;
        this.registry = registry;
        this.context = new SessionContext(socket);
    }

    @Override
    public void run() {
        LOGGER.info("Клиент подключён: " + context.getRemoteAddress());
        try (Socket client = socket;
             ObjectOutputStream output = new ObjectOutputStream(client.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(client.getInputStream())) {
            output.flush();
            client.setTcpNoDelay(true);
            while (!client.isClosed()) {
                Object object = input.readObject();
                if (!(object instanceof Request request)) {
                    break;
                }
                Response response = registry.dispatch(request, context);
                output.writeObject(response);
                output.flush();
                output.reset();
                if (Command.PING.equals(request.getCommand())) {
                    continue;
                }
                LOGGER.fine(() -> "Запрос " + request.getCommand() + " от "
                        + (context.getUser() == null ? "гостя" : context.getUser().getUsername()));
            }
        } catch (EOFException | SocketException exception) {
            LOGGER.fine("Клиент отключился: " + context.getRemoteAddress());
        } catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Ошибка ввода-вывода у клиента " + context.getRemoteAddress(),
                    exception);
        } catch (ClassNotFoundException exception) {
            LOGGER.log(Level.WARNING, "Получен неизвестный объект от клиента", exception);
        } finally {
            LOGGER.info("Сессия завершена: " + context.getRemoteAddress());
        }
    }
}
