package com.airport.server.dao;

import com.airport.common.exception.DataAccessException;
import com.airport.common.model.Booking;
import com.airport.common.model.BookingStatus;
import com.airport.common.model.TicketClass;
import com.airport.server.db.ConnectionPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcBookingDao extends AbstractJdbcDao implements BookingDao {

    private static final String SELECT = """
            SELECT b.id, b.flight_id, b.passenger_id, b.seat_number, b.ticket_class, b.price,
                   b.booking_time, b.booking_status, b.created_by,
                   f.flight_number, f.destination_city, a.name AS airline_name,
                   CONCAT(p.last_name, ' ', p.first_name) AS passenger_name
              FROM bookings b
              JOIN flights f ON f.id = b.flight_id
              JOIN airlines a ON a.id = f.airline_id
              JOIN passengers p ON p.id = b.passenger_id
            """;

    public JdbcBookingDao(ConnectionPool pool) {
        super(pool);
    }

    @Override
    public List<Booking> search(BookingFilter filter) throws DataAccessException {
        StringBuilder sql = new StringBuilder(SELECT).append(" WHERE 1=1");
        List<Object> parameters = new ArrayList<>();
        if (filter != null) {
            if (filter.getFlightId() != null) {
                sql.append(" AND b.flight_id = ?");
                parameters.add(filter.getFlightId());
            }
            if (filter.getPassengerId() != null) {
                sql.append(" AND b.passenger_id = ?");
                parameters.add(filter.getPassengerId());
            }
            if (filter.getStatus() != null) {
                sql.append(" AND b.booking_status = ?");
                parameters.add(filter.getStatus());
            }
            if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
                String pattern = "%" + filter.getSearch().trim().toLowerCase() + "%";
                sql.append(" AND (LOWER(f.flight_number) LIKE ? OR LOWER(p.last_name) LIKE ?"
                        + " OR LOWER(p.first_name) LIKE ? OR LOWER(p.passport_number) LIKE ?"
                        + " OR LOWER(b.seat_number) LIKE ?)");
                parameters.add(pattern);
                parameters.add(pattern);
                parameters.add(pattern);
                parameters.add(pattern);
                parameters.add(pattern);
            }
        }
        sql.append(" ORDER BY b.booking_time DESC");
        return queryList(sql.toString(), this::map, parameters.toArray());
    }

    @Override
    public Optional<Booking> findById(long id) throws DataAccessException {
        return queryOne(SELECT + " WHERE b.id = ?", this::map, id);
    }

    @Override
    public long insert(Booking booking) throws DataAccessException {
        return insert("INSERT INTO bookings (flight_id, passenger_id, seat_number, ticket_class,"
                        + " price, booking_time, booking_status, created_by)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                booking.getFlightId(), booking.getPassengerId(), booking.getSeatNumber(),
                booking.getTicketClass(), booking.getPrice(), booking.getBookingTime(),
                booking.getStatus(), booking.getCreatedBy());
    }

    @Override
    public void update(Booking booking) throws DataAccessException {
        int rows = update("UPDATE bookings SET flight_id = ?, passenger_id = ?, seat_number = ?,"
                        + " ticket_class = ?, price = ?, booking_status = ? WHERE id = ?",
                booking.getFlightId(), booking.getPassengerId(), booking.getSeatNumber(),
                booking.getTicketClass(), booking.getPrice(), booking.getStatus(), booking.getId());
        if (rows == 0) {
            throw new DataAccessException("Бронирование с id=" + booking.getId() + " не найдено");
        }
    }

    @Override
    public void delete(long id) throws DataAccessException {
        update("DELETE FROM bookings WHERE id = ?", id);
    }

    @Override
    public boolean existsSeat(long flightId, String seatNumber, Long excludeBookingId)
            throws DataAccessException {
        long exclude = excludeBookingId == null ? -1L : excludeBookingId;
        return queryLong("SELECT COUNT(*) FROM bookings WHERE flight_id = ?"
                        + " AND UPPER(seat_number) = UPPER(?) AND booking_status <> 'CANCELLED'"
                        + " AND id <> ?",
                flightId, seatNumber, exclude) > 0;
    }

    @Override
    public long countActiveByFlight(long flightId) throws DataAccessException {
        return queryLong("SELECT COUNT(*) FROM bookings WHERE flight_id = ?"
                + " AND booking_status <> 'CANCELLED'", flightId);
    }

    @Override
    public long countActive() throws DataAccessException {
        return queryLong("SELECT COUNT(*) FROM bookings WHERE booking_status <> 'CANCELLED'");
    }

    private Booking map(java.sql.ResultSet resultSet) throws java.sql.SQLException {
        Booking booking = new Booking();
        booking.setId(resultSet.getLong("id"));
        booking.setFlightId(resultSet.getLong("flight_id"));
        booking.setPassengerId(resultSet.getLong("passenger_id"));
        booking.setSeatNumber(resultSet.getString("seat_number"));
        booking.setTicketClass(TicketClass.fromString(resultSet.getString("ticket_class")));
        booking.setPrice(resultSet.getBigDecimal("price"));
        booking.setBookingTime(getDateTime(resultSet, "booking_time"));
        booking.setStatus(BookingStatus.fromString(resultSet.getString("booking_status")));
        booking.setCreatedBy(resultSet.getLong("created_by"));
        booking.setFlightNumber(resultSet.getString("flight_number"));
        booking.setDestinationCity(resultSet.getString("destination_city"));
        booking.setAirlineName(resultSet.getString("airline_name"));
        booking.setPassengerName(resultSet.getString("passenger_name"));
        return booking;
    }
}
