package com.airport.server.dao;

import com.airport.common.exception.DataAccessException;
import com.airport.common.model.Flight;

import java.util.List;
import java.util.Optional;

public interface FlightDao {

    List<Flight> search(FlightFilter filter) throws DataAccessException;

    Optional<Flight> findById(long id) throws DataAccessException;

    Optional<Flight> findByNumber(String flightNumber) throws DataAccessException;

    long insert(Flight flight) throws DataAccessException;

    void update(Flight flight) throws DataAccessException;

    void delete(long id) throws DataAccessException;

    long countBookings(long flightId) throws DataAccessException;

    long count() throws DataAccessException;
}
