package com.airport.server.dao;

import com.airport.common.exception.DataAccessException;
import com.airport.common.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {

    Optional<User> findByUsername(String username) throws DataAccessException;

    Optional<User> findById(long id) throws DataAccessException;

    List<User> findAll() throws DataAccessException;

    long insert(User user) throws DataAccessException;

    void update(User user) throws DataAccessException;

    void delete(long id) throws DataAccessException;

    long countAdmins() throws DataAccessException;

    long countCreatedBookings(long userId) throws DataAccessException;

    long count() throws DataAccessException;
}
