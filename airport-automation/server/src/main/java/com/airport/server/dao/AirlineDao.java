package com.airport.server.dao;

import com.airport.common.exception.DataAccessException;
import com.airport.common.model.Airline;

import java.util.List;
import java.util.Optional;

public interface AirlineDao {

    List<Airline> findAll() throws DataAccessException;

    Optional<Airline> findById(long id) throws DataAccessException;

    Optional<Airline> findByIataCode(String code) throws DataAccessException;

    long insert(Airline airline) throws DataAccessException;

    void update(Airline airline) throws DataAccessException;

    void delete(long id) throws DataAccessException;

    long countFlights(long airlineId) throws DataAccessException;
}
