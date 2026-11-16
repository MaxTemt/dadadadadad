package com.airport.server.dao;

import com.airport.common.exception.DataAccessException;
import com.airport.server.db.ConnectionPool;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Базовый JDBC-класс. Реализует шаблонный метод выполнения запросов
 * и централизованную привязку параметров.
 */
public abstract class AbstractJdbcDao {

    protected final ConnectionPool pool;

    protected AbstractJdbcDao(ConnectionPool pool) {
        this.pool = pool;
    }

    protected <T> List<T> queryList(String sql, RowMapper<T> mapper, Object... parameters)
            throws DataAccessException {
        return pool.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bind(statement, parameters);
                try (ResultSet resultSet = statement.executeQuery()) {
                    List<T> result = new ArrayList<>();
                    while (resultSet.next()) {
                        result.add(mapper.map(resultSet));
                    }
                    return result;
                }
            }
        });
    }

    protected <T> Optional<T> queryOne(String sql, RowMapper<T> mapper, Object... parameters)
            throws DataAccessException {
        List<T> result = queryList(sql, mapper, parameters);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    protected long queryLong(String sql, Object... parameters) throws DataAccessException {
        return pool.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bind(statement, parameters);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? resultSet.getLong(1) : 0L;
                }
            }
        });
    }

    protected double queryDouble(String sql, Object... parameters) throws DataAccessException {
        return pool.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bind(statement, parameters);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? resultSet.getDouble(1) : 0.0;
                }
            }
        });
    }

    protected int update(String sql, Object... parameters) throws DataAccessException {
        return pool.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bind(statement, parameters);
                return statement.executeUpdate();
            }
        });
    }

    protected long insert(String sql, Object... parameters) throws DataAccessException {
        return pool.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql,
                    Statement.RETURN_GENERATED_KEYS)) {
                bind(statement, parameters);
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    return keys.next() ? keys.getLong(1) : 0L;
                }
            }
        });
    }

    protected static void bind(PreparedStatement statement, Object... parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            Object value = parameters[i];
            int index = i + 1;
            if (value == null) {
                statement.setNull(index, Types.NULL);
            } else if (value instanceof LocalDateTime dateTime) {
                statement.setTimestamp(index, Timestamp.valueOf(dateTime));
            } else if (value instanceof LocalDate date) {
                statement.setDate(index, Date.valueOf(date));
            } else if (value instanceof Enum<?> enumeration) {
                statement.setString(index, enumeration.name());
            } else if (value instanceof BigDecimal decimal) {
                statement.setBigDecimal(index, decimal);
            } else {
                statement.setObject(index, value);
            }
        }
    }

    protected static LocalDateTime getDateTime(ResultSet resultSet, String column) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp(column);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    protected static LocalDate getDate(ResultSet resultSet, String column) throws SQLException {
        Date date = resultSet.getDate(column);
        return date == null ? null : date.toLocalDate();
    }
}
