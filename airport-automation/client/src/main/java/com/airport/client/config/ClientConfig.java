package com.airport.client.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Параметры подключения клиента. Могут быть заданы в client.properties
 * или аргументами --host / --port.
 */
public final class ClientConfig {

    private final String host;
    private final int port;

    public ClientConfig(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static ClientConfig load(String[] args) throws IOException {
        Properties properties = new Properties();
        try (InputStream stream = ClientConfig.class.getResourceAsStream("/client.properties")) {
            if (stream != null) {
                properties.load(stream);
            }
        }
        Path file = Path.of("client.properties");
        if (Files.isRegularFile(file)) {
            try (InputStream stream = Files.newInputStream(file)) {
                properties.load(stream);
            }
        }
        String host = properties.getProperty("server.host", "localhost");
        int port = Integer.parseInt(properties.getProperty("server.port", "5555").trim());
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--host".equals(arg) && i + 1 < args.length) {
                host = args[++i];
            } else if (arg.startsWith("--host=")) {
                host = arg.substring("--host=".length());
            } else if ("--port".equals(arg) && i + 1 < args.length) {
                port = Integer.parseInt(args[++i]);
            } else if (arg.startsWith("--port=")) {
                port = Integer.parseInt(arg.substring("--port=".length()));
            }
        }
        return new ClientConfig(host, port);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
