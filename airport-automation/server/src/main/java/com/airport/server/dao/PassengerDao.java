package com.airport.server.dao;

import com.airport.common.exception.DataAccessException;
import com.airport.common.model.Passenger;

import java.util.List;
import java.util.Optional;

public interface PassengerDao {

    List<Passenger> search(String search) throws DataAccessException;

    Optional<Passenger> findById(long id) throws DataAccessException;

    Optional<Passenger> findByPassport(String passport) throws DataAccessException;

    long insert(Passenger passenger) throws DataAccessException;

    void update(Passenger passenger) throws DataAccessException;

    void delete(long id) throws DataAccessException;

    long countBookings(long passengerId) throws DataAccessException;

    long count() throws DataAccessException;
}
