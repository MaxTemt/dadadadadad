package com.airport.server.dao;

import com.airport.common.exception.DataAccessException;
import com.airport.common.model.Flight;
import com.airport.common.model.FlightStatus;
import com.airport.server.db.ConnectionPool;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcFlightDao extends AbstractJdbcDao implements FlightDao {

    private static final String SELECT = """
            SELECT f.id, f.flight_number, f.airline_id, f.origin_city, f.destination_city,
                   f.departure_time, f.arrival_time, f.gate_number, f.aircraft_type,
                   f.total_seats, f.flight_status, f.base_price, a.name AS airline_name,
                   (SELECT COUNT(*) FROM bookings b
                     WHERE b.flight_id = f.id AND b.booking_status <> 'CANCELLED') AS booked_seats
              FROM flights f
              JOIN airlines a ON a.id = f.airline_id
            """;

    public JdbcFlightDao(ConnectionPool pool) {
        super(pool);
    }

    @Override
    public List<Flight> search(FlightFilter filter) throws DataAccessException {
        StringBuilder sql = new StringBuilder(SELECT).append(" WHERE 1=1");
        List<Object> parameters = new ArrayList<>();
        if (filter != null) {
            if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
                String pattern = "%" + filter.getSearch().trim().toLowerCase() + "%";
                sql.append(" AND (LOWER(f.flight_number) LIKE ? OR LOWER(f.origin_city) LIKE ?"
                        + " OR LOWER(f.destination_city) LIKE ? OR LOWER(a.name) LIKE ?)");
                parameters.add(pattern);
                parameters.add(pattern);
                parameters.add(pattern);
                parameters.add(pattern);
            }
            if (filter.getStatus() != null) {
                sql.append(" AND f.flight_status = ?");
                parameters.add(filter.getStatus());
            }
            if (filter.getDate() != null) {
                sql.append(" AND f.departure_time >= ? AND f.departure_time < ?");
                parameters.add(filter.getDate().atStartOfDay());
                parameters.add(filter.getDate().plusDays(1).atStartOfDay());
            }
        }
        sql.append(" ORDER BY f.departure_time");
        return queryList(sql.toString(), this::map, parameters.toArray());
    }

    @Override
    public Optional<Flight> findById(long id) throws DataAccessException {
        return queryOne(SELECT + " WHERE f.id = ?", this::map, id);
    }

    @Override
    public Optional<Flight> findByNumber(String flightNumber) throws DataAccessException {
        return queryOne(SELECT + " WHERE UPPER(f.flight_number) = UPPER(?)", this::map, flightNumber);
    }

    @Override
    public long insert(Flight flight) throws DataAccessException {
        return insert("INSERT INTO flights (flight_number, airline_id, origin_city, destination_city,"
                        + " departure_time, arrival_time, gate_number, aircraft_type, total_seats,"
                        + " flight_status, base_price) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                flight.getFlightNumber(), flight.getAirlineId(), flight.getOriginCity(),
                flight.getDestinationCity(), flight.getDepartureTime(), flight.getArrivalTime(),
                flight.getGate(), flight.getAircraftType(), flight.getTotalSeats(),
                flight.getStatus(), flight.getBasePrice());
    }

    @Override
    public void update(Flight flight) throws DataAccessException {
        int rows = update("UPDATE flights SET flight_number = ?, airline_id = ?, origin_city = ?,"
                        + " destination_city = ?, departure_time = ?, arrival_time = ?, gate_number = ?,"
                        + " aircraft_type = ?, total_seats = ?, flight_status = ?, base_price = ?"
                        + " WHERE id = ?",
                flight.getFlightNumber(), flight.getAirlineId(), flight.getOriginCity(),
                flight.getDestinationCity(), flight.getDepartureTime(), flight.getArrivalTime(),
                flight.getGate(), flight.getAircraftType(), flight.getTotalSeats(),
                flight.getStatus(), flight.getBasePrice(), flight.getId());
        if (rows == 0) {
            throw new DataAccessException("Рейс с id=" + flight.getId() + " не найден");
        }
    }

    @Override
    public void delete(long id) throws DataAccessException {
        update("DELETE FROM flights WHERE id = ?", id);
    }

    @Override
    public long countBookings(long flightId) throws DataAccessException {
        return queryLong("SELECT COUNT(*) FROM bookings WHERE flight_id = ?", flightId);
    }

    @Override
    public long count() throws DataAccessException {
        return queryLong("SELECT COUNT(*) FROM flights");
    }

    private Flight map(java.sql.ResultSet resultSet) throws java.sql.SQLException {
        Flight flight = new Flight();
        flight.setId(resultSet.getLong("id"));
        flight.setFlightNumber(resultSet.getString("flight_number"));
        flight.setAirlineId(resultSet.getLong("airline_id"));
        flight.setOriginCity(resultSet.getString("origin_city"));
        flight.setDestinationCity(resultSet.getString("destination_city"));
        flight.setDepartureTime(getDateTime(resultSet, "departure_time"));
        flight.setArrivalTime(getDateTime(resultSet, "arrival_time"));
        flight.setGate(resultSet.getString("gate_number"));
        flight.setAircraftType(resultSet.getString("aircraft_type"));
        flight.setTotalSeats(resultSet.getInt("total_seats"));
        flight.setStatus(FlightStatus.fromString(resultSet.getString("flight_status")));
        flight.setBasePrice(resultSet.getBigDecimal("base_price"));
        flight.setAirlineName(resultSet.getString("airline_name"));
        flight.setBookedSeats(resultSet.getInt("booked_seats"));
        return flight;
    }
}
