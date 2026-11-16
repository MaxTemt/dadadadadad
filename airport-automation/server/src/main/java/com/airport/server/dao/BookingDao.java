package com.airport.server.dao;

import com.airport.common.exception.DataAccessException;
import com.airport.common.model.Booking;

import java.util.List;
import java.util.Optional;

public interface BookingDao {

    List<Booking> search(BookingFilter filter) throws DataAccessException;

    Optional<Booking> findById(long id) throws DataAccessException;

    long insert(Booking booking) throws DataAccessException;

    void update(Booking booking) throws DataAccessException;

    void delete(long id) throws DataAccessException;

    boolean existsSeat(long flightId, String seatNumber, Long excludeBookingId) throws DataAccessException;

    long countActiveByFlight(long flightId) throws DataAccessException;

    long countActive() throws DataAccessException;
}
