package com.airport.server;

import com.airport.server.config.ServerConfig;
import com.airport.server.net.AirportServer;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Точка входа серверного приложения.
 */
public final class ServerMain {

    private static final Logger LOGGER = Logger.getLogger(ServerMain.class.getName());

    private ServerMain() {
    }

    public static void main(String[] args) {
        try {
            ServerConfig config = ServerConfig.load(args);
            if (config.isHelpRequested()) {
                ServerConfig.printUsage();
                return;
            }

            ServerContext context = ServerContext.create(config);
            AirportServer server = new AirportServer(config, context.getCommandRegistry());
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                server.close();
                context.close();
            }, "airport-shutdown"));

            server.start();
            System.out.println("==========================================================");
            System.out.println("  Система автоматизации работы аэропорта — сервер");
            System.out.println("  Адрес:        " + config.getHost() + ":" + server.getPort());
            System.out.println("  База данных:  " + config.getDbUrl());
            System.out.println("  Потоков:      " + config.getMaxThreads());
            System.out.println("  Соединений:   " + config.getPoolSize());
            System.out.println("  Отчёты:       " + config.getReportsDir().toAbsolutePath());
            System.out.println("  Копии:        " + config.getBackupsDir().toAbsolutePath());
            System.out.println("  Для остановки нажмите Ctrl+C");
            System.out.println("==========================================================");
            server.await();
        } catch (IllegalArgumentException exception) {
            System.err.println("Ошибка конфигурации: " + exception.getMessage());
            ServerConfig.printUsage();
            System.exit(2);
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Не удалось запустить сервер", exception);
            System.err.println("Не удалось запустить сервер: " + exception.getMessage());
            System.err.println("Проверьте, что MySQL запущен и схема создана командой:");
            System.err.println("  mysql -u root -p < db/schema.sql");
            System.exit(1);
        }
    }
}
