package com.airport.server.net;

import com.airport.server.config.ServerConfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Многопоточный TCP-сервер. Каждое подключение обслуживается в отдельном потоке
 * из фиксированного пула, что обеспечивает параллельную обработку запросов.
 */
public final class AirportServer implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(AirportServer.class.getName());

    private final ServerConfig config;
    private final CommandRegistry registry;
    private final ExecutorService clients;
    private ServerSocket serverSocket;
    private Thread acceptThread;
    private volatile boolean running;

    public AirportServer(ServerConfig config, CommandRegistry registry) {
        this.config = config;
        this.registry = registry;
        AtomicInteger counter = new AtomicInteger();
        this.clients = Executors.newFixedThreadPool(config.getMaxThreads(), runnable -> {
            Thread thread = new Thread(runnable, "airport-client-" + counter.incrementAndGet());
            thread.setDaemon(false);
            return thread;
        });
    }

    public synchronized void start() throws IOException {
        if (running) {
            throw new IllegalStateException("Сервер уже запущен");
        }
        serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(config.getHost(), config.getPort()));
        running = true;
        acceptThread = new Thread(this::acceptLoop, "airport-acceptor");
        acceptThread.start();
        LOGGER.info("Сервер запущен на " + config.getHost() + ":" + getPort());
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                socket.setKeepAlive(true);
                clients.submit(new ClientSession(socket, registry));
            } catch (SocketException exception) {
                if (running) {
                    LOGGER.log(Level.WARNING, "Ошибка сокета сервера", exception);
                }
                break;
            } catch (IOException exception) {
                if (running) {
                    LOGGER.log(Level.WARNING, "Ошибка приёма подключения", exception);
                }
            }
        }
    }

    public void await() {
        Thread thread = acceptThread;
        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public int getPort() {
        return serverSocket == null ? config.getPort() : serverSocket.getLocalPort();
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public synchronized void close() {
        running = false;
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException exception) {
                LOGGER.log(Level.FINE, "Ошибка закрытия серверного сокета", exception);
            }
        }
        clients.shutdownNow();
        try {
            clients.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
        LOGGER.info("Сервер остановлен");
    }
}
