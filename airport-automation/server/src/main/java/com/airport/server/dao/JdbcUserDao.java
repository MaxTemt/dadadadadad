package com.airport.server.dao;

import com.airport.common.exception.DataAccessException;
import com.airport.common.model.Role;
import com.airport.common.model.User;
import com.airport.server.db.ConnectionPool;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class JdbcUserDao extends AbstractJdbcDao implements UserDao {

    private static final String COLUMNS =
            "id, username, password_hash, user_role, first_name, last_name, email, phone, active, created_at";

    public JdbcUserDao(ConnectionPool pool) {
        super(pool);
    }

    @Override
    public Optional<User> findByUsername(String username) throws DataAccessException {
        return queryOne("SELECT " + COLUMNS + " FROM users WHERE LOWER(username) = LOWER(?)",
                this::map, username);
    }

    @Override
    public Optional<User> findById(long id) throws DataAccessException {
        return queryOne("SELECT " + COLUMNS + " FROM users WHERE id = ?", this::map, id);
    }

    @Override
    public List<User> findAll() throws DataAccessException {
        return queryList("SELECT " + COLUMNS + " FROM users ORDER BY last_name, first_name",
                this::map);
    }

    @Override
    public long insert(User user) throws DataAccessException {
        return insert("INSERT INTO users (username, password_hash, user_role, first_name, last_name,"
                        + " email, phone, active, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                user.getUsername(), user.getPasswordHash(), user.getRole(), user.getFirstName(),
                user.getLastName(), user.getEmail(), user.getPhone(), user.isActive(),
                user.getCreatedAt() == null ? LocalDateTime.now() : user.getCreatedAt());
    }

    @Override
    public void update(User user) throws DataAccessException {
        int rows = update("UPDATE users SET username = ?, password_hash = ?, user_role = ?,"
                        + " first_name = ?, last_name = ?, email = ?, phone = ?, active = ? WHERE id = ?",
                user.getUsername(), user.getPasswordHash(), user.getRole(), user.getFirstName(),
                user.getLastName(), user.getEmail(), user.getPhone(), user.isActive(), user.getId());
        if (rows == 0) {
            throw new DataAccessException("Пользователь с id=" + user.getId() + " не найден");
        }
    }

    @Override
    public void delete(long id) throws DataAccessException {
        update("DELETE FROM users WHERE id = ?", id);
    }

    @Override
    public long countAdmins() throws DataAccessException {
        return queryLong("SELECT COUNT(*) FROM users WHERE user_role = 'ADMIN' AND active = TRUE");
    }

    @Override
    public long countCreatedBookings(long userId) throws DataAccessException {
        return queryLong("SELECT COUNT(*) FROM bookings WHERE created_by = ?", userId);
    }

    @Override
    public long count() throws DataAccessException {
        return queryLong("SELECT COUNT(*) FROM users");
    }

    private User map(java.sql.ResultSet resultSet) throws java.sql.SQLException {
        User user = new User();
        user.setId(resultSet.getLong("id"));
        user.setUsername(resultSet.getString("username"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setRole(Role.fromString(resultSet.getString("user_role")));
        user.setFirstName(resultSet.getString("first_name"));
        user.setLastName(resultSet.getString("last_name"));
        user.setEmail(resultSet.getString("email"));
        user.setPhone(resultSet.getString("phone"));
        user.setActive(resultSet.getBoolean("active"));
        user.setCreatedAt(getDateTime(resultSet, "created_at"));
        return user;
    }
}
