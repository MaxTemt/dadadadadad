package com.airport.server.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Функциональный интерфейс преобразования строки ResultSet в объект.
 */
@FunctionalInterface
public interface RowMapper<T> {
    T map(ResultSet resultSet) throws SQLException;
}
