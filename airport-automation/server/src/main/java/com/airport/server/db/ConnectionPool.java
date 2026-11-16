package com.airport.server.db;

import com.airport.common.exception.DataAccessException;
import com.airport.server.config.ServerConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Пул соединений с БД. Реализует повторное использование соединений
 * и корректное освобождение ресурсов. Является общим (singleton-подобным)
 * компонентом в рамках серверного приложения.
 */
public final class ConnectionPool implements AutoCloseable {

    @FunctionalInterface
    public interface SqlWork<T> {
        T run(Connection connection) throws SQLException;
    }

    private final String url;
    private final String user;
    private final String password;
    private final long timeoutMs;
    private final BlockingQueue<Connection> available;
    private final List<Connection> allConnections = new ArrayList<>();
    private volatile boolean closed;

    public ConnectionPool(ServerConfig config) throws DataAccessException {
        this(config.getDbUrl(), config.getDbUser(), config.getDbPassword(),
                config.getPoolSize(), config.getPoolTimeoutMs());
    }

    public ConnectionPool(String url, String user, String password, int size, long timeoutMs)
            throws DataAccessException {
        this.url = url;
        this.user = user;
        this.password = password;
        this.timeoutMs = timeoutMs;
        this.available = new ArrayBlockingQueue<>(size);
        try {
            for (int i = 0; i < size; i++) {
                Connection connection = openConnection();
                available.offer(connection);
                allConnections.add(connection);
            }
        } catch (SQLException exception) {
            closeQuietly();
            throw new DataAccessException("Не удалось подключиться к базе данных. "
                    + "Проверьте параметры JDBC и наличие схемы (db/schema.sql).", exception);
        }
    }

    private Connection openConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(url, user, password);
        connection.setAutoCommit(true);
        return connection;
    }

    public Connection borrow() throws DataAccessException {
        if (closed) {
            throw new DataAccessException("Пул соединений закрыт");
        }
        try {
            Connection connection = available.poll(timeoutMs, TimeUnit.MILLISECONDS);
            if (connection == null) {
                throw new DataAccessException("Истекло время ожидания свободного соединения с БД");
            }
            if (connection.isClosed() || !connection.isValid(2)) {
                replace(connection);
                connection = available.poll(timeoutMs, TimeUnit.MILLISECONDS);
                if (connection == null) {
                    throw new DataAccessException("Не удалось получить рабочее соединение с БД");
                }
            }
            return connection;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new DataAccessException("Ожидание соединения с БД прервано", exception);
        } catch (SQLException exception) {
            throw new DataAccessException("Ошибка при проверке соединения с БД", exception);
        }
    }

    public void release(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            if (closed || connection.isClosed()) {
                closeQuietly(connection);
                return;
            }
            if (!connection.getAutoCommit()) {
                connection.rollback();
                connection.setAutoCommit(true);
            }
            if (!available.offer(connection)) {
                closeQuietly(connection);
            }
        } catch (SQLException exception) {
            closeQuietly(connection);
        }
    }

    private void replace(Connection broken) {
        closeQuietly(broken);
        synchronized (allConnections) {
            allConnections.remove(broken);
            try {
                Connection fresh = openConnection();
                allConnections.add(fresh);
                if (!available.offer(fresh)) {
                    closeQuietly(fresh);
                }
            } catch (SQLException exception) {
                // соединение будет восстановлено при следующем обращении
            }
        }
    }

    public <T> T withConnection(SqlWork<T> work) throws DataAccessException {
        Connection connection = borrow();
        try {
            return work.run(connection);
        } catch (SQLException exception) {
            throw new DataAccessException("Ошибка выполнения запроса к базе данных: "
                    + exception.getMessage(), exception);
        } finally {
            release(connection);
        }
    }

    public <T> T withTransaction(SqlWork<T> work) throws DataAccessException {
        Connection connection = borrow();
        try {
            connection.setAutoCommit(false);
            T result = work.run(connection);
            connection.commit();
            return result;
        } catch (SQLException exception) {
            try {
                connection.rollback();
            } catch (SQLException rollbackException) {
                exception.addSuppressed(rollbackException);
            }
            throw new DataAccessException("Ошибка транзакции: " + exception.getMessage(), exception);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignored) {
                closeQuietly(connection);
            }
            release(connection);
        }
    }

    @Override
    public void close() {
        closed = true;
        closeQuietly();
    }

    private void closeQuietly() {
        synchronized (allConnections) {
            for (Connection connection : allConnections) {
                closeQuietly(connection);
            }
            allConnections.clear();
            available.clear();
        }
    }

    private static void closeQuietly(Connection connection) {
        try {
            connection.close();
        } catch (SQLException ignored) {
            // nothing
        }
    }
}
