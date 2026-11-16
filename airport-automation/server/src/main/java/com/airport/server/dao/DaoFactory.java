package com.airport.server.dao;

import com.airport.server.db.ConnectionPool;

/**
 * Фабрика DAO. Скрывает создание конкретных реализаций от сервисного слоя.
 */
public class DaoFactory {

    private final UserDao userDao;
    private final AirlineDao airlineDao;
    private final PassengerDao passengerDao;
    private final FlightDao flightDao;
    private final BookingDao bookingDao;
    private final AnalyticsDao analyticsDao;

    public DaoFactory(ConnectionPool pool) {
        this.userDao = new JdbcUserDao(pool);
        this.airlineDao = new JdbcAirlineDao(pool);
        this.passengerDao = new JdbcPassengerDao(pool);
        this.flightDao = new JdbcFlightDao(pool);
        this.bookingDao = new JdbcBookingDao(pool);
        this.analyticsDao = new AnalyticsDao(pool);
    }

    public UserDao users() {
        return userDao;
    }

    public AirlineDao airlines() {
        return airlineDao;
    }

    public PassengerDao passengers() {
        return passengerDao;
    }

    public FlightDao flights() {
        return flightDao;
    }

    public BookingDao bookings() {
        return bookingDao;
    }

    public AnalyticsDao analytics() {
        return analyticsDao;
    }
}
