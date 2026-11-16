package com.airport.client.net;

import com.airport.common.exception.AirportException;
import com.airport.common.net.Request;
import com.airport.common.net.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * TCP-соединение клиента с сервером. Все обмены синхронизированы,
 * что позволяет безопасно использовать одно соединение из разных потоков.
 */
public class ServerConnection {

    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String remote;

    public void connect(String host, int port) throws AirportException {
        close();
        try {
            Socket newSocket = new Socket();
            newSocket.connect(new InetSocketAddress(host, port), 7000);
            newSocket.setSoTimeout(60_000);
            newSocket.setTcpNoDelay(true);
            ObjectOutputStream newOutput = new ObjectOutputStream(newSocket.getOutputStream());
            newOutput.flush();
            ObjectInputStream newInput = new ObjectInputStream(newSocket.getInputStream());
            this.socket = newSocket;
            this.output = newOutput;
            this.input = newInput;
            this.remote = host + ":" + port;
        } catch (IOException exception) {
            close();
            throw new AirportException("NETWORK", "Не удалось подключиться к серверу " + host + ":" + port
                    + ". Проверьте, что сервер запущен.", exception);
        }
    }

    public synchronized Response send(Request request) throws AirportException {
        if (socket == null || socket.isClosed()) {
            throw new AirportException("NETWORK", "Соединение с сервером не установлено");
        }
        try {
            output.writeObject(request);
            output.flush();
            output.reset();
            Object response = input.readObject();
            if (!(response instanceof Response result)) {
                throw new AirportException("NETWORK", "Некорректный ответ сервера");
            }
            return result;
        } catch (IOException | ClassNotFoundException exception) {
            close();
            throw new AirportException("NETWORK", "Связь с сервером потеряна: " + exception.getMessage(),
                    exception);
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public String getRemote() {
        return remote;
    }

    public void close() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
                // соединение уже закрыто
            }
        }
        socket = null;
        output = null;
        input = null;
    }
}
