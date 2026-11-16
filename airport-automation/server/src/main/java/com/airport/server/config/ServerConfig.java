package com.airport.server.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Конфигурация сервера. Загружается из classpath, файла свойств и аргументов командной строки.
 * Значения из командной строки имеют наивысший приоритет.
 */
public final class ServerConfig {

    public static final int DEFAULT_PORT = 5555;

    private final String host;
    private final int port;
    private final int maxThreads;
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;
    private final int poolSize;
    private final long poolTimeoutMs;
    private final Path reportsDir;
    private final Path backupsDir;
    private final boolean helpRequested;

    private ServerConfig(Builder builder) {
        this.host = builder.host;
        this.port = builder.port;
        this.maxThreads = builder.maxThreads;
        this.dbUrl = builder.dbUrl;
        this.dbUser = builder.dbUser;
        this.dbPassword = builder.dbPassword;
        this.poolSize = builder.poolSize;
        this.poolTimeoutMs = builder.poolTimeoutMs;
        this.reportsDir = Path.of(builder.reportsDir);
        this.backupsDir = Path.of(builder.backupsDir);
        this.helpRequested = builder.help;
    }

    public static ServerConfig load(String[] args) throws IOException {
        Properties properties = new Properties();
        try (InputStream stream = ServerConfig.class.getResourceAsStream("/server.properties")) {
            if (stream != null) {
                properties.load(stream);
            }
        }

        Path configPath = null;
        List<String> overrides = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--help".equals(arg) || "-h".equals(arg)) {
                overrides.add("help=true");
                continue;
            }
            if (arg.startsWith("--config=")) {
                configPath = Path.of(arg.substring("--config=".length()));
            } else if ("--config".equals(arg) && i + 1 < args.length) {
                configPath = Path.of(args[++i]);
            } else {
                overrides.add(arg);
            }
        }

        if (configPath == null && Files.isRegularFile(Path.of("server.properties"))) {
            configPath = Path.of("server.properties");
        }
        if (configPath != null) {
            if (!Files.isRegularFile(configPath)) {
                throw new IOException("Файл конфигурации не найден: " + configPath.toAbsolutePath());
            }
            try (InputStream stream = Files.newInputStream(configPath)) {
                properties.load(stream);
            }
        }

        Builder builder = new Builder()
                .host(properties.getProperty("server.host", "0.0.0.0"))
                .port(parseInt(properties, "server.port", DEFAULT_PORT))
                .maxThreads(parseInt(properties, "server.threads", 16))
                .dbUrl(properties.getProperty("db.url",
                        "jdbc:mysql://localhost:3306/airport?useSSL=false"
                                + "&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8"))
                .dbUser(properties.getProperty("db.user", "airport"))
                .dbPassword(properties.getProperty("db.password", "airport"))
                .poolSize(parseInt(properties, "db.pool.size", 10))
                .poolTimeoutMs(parseLong(properties, "db.pool.timeout.ms", 10_000))
                .reportsDir(properties.getProperty("reports.dir", "reports"))
                .backupsDir(properties.getProperty("backup.dir", "backups"));

        for (int i = 0; i < overrides.size(); i++) {
            String arg = overrides.get(i);
            if ("help=true".equals(arg)) {
                builder.help(true);
                continue;
            }
            if (!arg.startsWith("--")) {
                throw new IllegalArgumentException("Неизвестный аргумент: " + arg);
            }
            String key;
            String value;
            int equals = arg.indexOf('=');
            if (equals > 0) {
                key = arg.substring(2, equals);
                value = arg.substring(equals + 1);
            } else {
                key = arg.substring(2);
                if (i + 1 >= overrides.size()) {
                    throw new IllegalArgumentException("Не указано значение для --" + key);
                }
                value = overrides.get(++i);
            }
            switch (key) {
                case "host" -> builder.host(value);
                case "port" -> builder.port(Integer.parseInt(value));
                case "threads" -> builder.maxThreads(Integer.parseInt(value));
                case "db-url" -> builder.dbUrl(value);
                case "db-user" -> builder.dbUser(value);
                case "db-password" -> builder.dbPassword(value);
                case "pool-size" -> builder.poolSize(Integer.parseInt(value));
                case "reports-dir" -> builder.reportsDir(value);
                case "backups-dir" -> builder.backupsDir(value);
                default -> throw new IllegalArgumentException("Неизвестный параметр: --" + key);
            }
        }

        return builder.build();
    }

    private static int parseInt(Properties properties, String key, int defaultValue) {
        String value = properties.getProperty(key);
        return value == null || value.isBlank() ? defaultValue : Integer.parseInt(value.trim());
    }

    private static long parseLong(Properties properties, String key, long defaultValue) {
        String value = properties.getProperty(key);
        return value == null || value.isBlank() ? defaultValue : Long.parseLong(value.trim());
    }

    public static void printUsage() {
        System.out.println("""
                Система автоматизации работы аэропорта — сервер

                Использование:
                  java -jar airport-server.jar [параметры]

                Параметры:
                  --config <файл>       файл свойств (по умолчанию server.properties)
                  --host <адрес>        адрес прослушивания (по умолчанию 0.0.0.0)
                  --port <порт>         TCP-порт сервера (по умолчанию 5555)
                  --threads <число>     размер пула обработчиков запросов
                  --db-url <url>        JDBC URL MySQL
                  --db-user <логин>     пользователь БД
                  --db-password <пароль> пароль БД
                  --pool-size <число>   размер пула соединений с БД
                  --reports-dir <путь>  каталог текстовых отчётов
                  --backups-dir <путь>  каталог резервных копий
                  --help                показать эту справку
                """);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public long getPoolTimeoutMs() {
        return poolTimeoutMs;
    }

    public Path getReportsDir() {
        return reportsDir;
    }

    public Path getBackupsDir() {
        return backupsDir;
    }

    public boolean isHelpRequested() {
        return helpRequested;
    }

    public static final class Builder {
        private String host = "0.0.0.0";
        private int port = DEFAULT_PORT;
        private int maxThreads = 16;
        private String dbUrl = "jdbc:mysql://localhost:3306/airport";
        private String dbUser = "airport";
        private String dbPassword = "airport";
        private int poolSize = 10;
        private long poolTimeoutMs = 10_000;
        private String reportsDir = "reports";
        private String backupsDir = "backups";
        private boolean help;

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder maxThreads(int maxThreads) {
            this.maxThreads = maxThreads;
            return this;
        }

        public Builder dbUrl(String dbUrl) {
            this.dbUrl = dbUrl;
            return this;
        }

        public Builder dbUser(String dbUser) {
            this.dbUser = dbUser;
            return this;
        }

        public Builder dbPassword(String dbPassword) {
            this.dbPassword = dbPassword;
            return this;
        }

        public Builder poolSize(int poolSize) {
            this.poolSize = poolSize;
            return this;
        }

        public Builder poolTimeoutMs(long poolTimeoutMs) {
            this.poolTimeoutMs = poolTimeoutMs;
            return this;
        }

        public Builder reportsDir(String reportsDir) {
            this.reportsDir = reportsDir;
            return this;
        }

        public Builder backupsDir(String backupsDir) {
            this.backupsDir = backupsDir;
            return this;
        }

        public Builder help(boolean help) {
            this.help = help;
            return this;
        }

        public boolean isHelp() {
            return help;
        }

        public ServerConfig build() {
            if (port < 0 || port > 65_535) {
                throw new IllegalArgumentException("Порт должен быть в диапазоне 0–65535 (0 — свободный порт)");
            }
            if (poolSize <= 0 || poolSize > 200) {
                throw new IllegalArgumentException("Размер пула соединений должен быть в диапазоне 1–200");
            }
            if (maxThreads <= 0 || maxThreads > 500) {
                throw new IllegalArgumentException("Размер пула потоков должен быть в диапазоне 1–500");
            }
            if (dbUrl == null || dbUrl.isBlank()) {
                throw new IllegalArgumentException("Не задан JDBC URL");
            }
            return new ServerConfig(this);
        }
    }
}
