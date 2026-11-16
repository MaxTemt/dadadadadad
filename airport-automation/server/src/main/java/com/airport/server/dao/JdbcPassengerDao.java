package com.airport.server.dao;

import com.airport.common.exception.DataAccessException;
import com.airport.common.model.Passenger;
import com.airport.server.db.ConnectionPool;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcPassengerDao extends AbstractJdbcDao implements PassengerDao {

    private static final String COLUMNS =
            "id, first_name, last_name, passport_number, email, phone, date_of_birth,"
                    + " frequent_flyer_level, created_at";

    public JdbcPassengerDao(ConnectionPool pool) {
        super(pool);
    }

    @Override
    public List<Passenger> search(String search) throws DataAccessException {
        StringBuilder sql = new StringBuilder("SELECT " + COLUMNS + " FROM passengers WHERE 1=1");
        List<Object> parameters = new ArrayList<>();
        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            sql.append(" AND (LOWER(last_name) LIKE ? OR LOWER(first_name) LIKE ?"
                    + " OR LOWER(passport_number) LIKE ? OR LOWER(COALESCE(email, '')) LIKE ?)");
            parameters.add(pattern);
            parameters.add(pattern);
            parameters.add(pattern);
            parameters.add(pattern);
        }
        sql.append(" ORDER BY last_name, first_name");
        return queryList(sql.toString(), this::map, parameters.toArray());
    }

    @Override
    public Optional<Passenger> findById(long id) throws DataAccessException {
        return queryOne("SELECT " + COLUMNS + " FROM passengers WHERE id = ?", this::map, id);
    }

    @Override
    public Optional<Passenger> findByPassport(String passport) throws DataAccessException {
        return queryOne("SELECT " + COLUMNS + " FROM passengers WHERE UPPER(passport_number) = UPPER(?)",
                this::map, passport);
    }

    @Override
    public long insert(Passenger passenger) throws DataAccessException {
        return insert("INSERT INTO passengers (first_name, last_name, passport_number, email, phone,"
                        + " date_of_birth, frequent_flyer_level, created_at)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                passenger.getFirstName(), passenger.getLastName(), passenger.getPassportNumber(),
                passenger.getEmail(), passenger.getPhone(), passenger.getDateOfBirth(),
                passenger.getFrequentFlyerLevel(),
                passenger.getCreatedAt() == null ? LocalDateTime.now() : passenger.getCreatedAt());
    }

    @Override
    public void update(Passenger passenger) throws DataAccessException {
        int rows = update("UPDATE passengers SET first_name = ?, last_name = ?, passport_number = ?,"
                        + " email = ?, phone = ?, date_of_birth = ?, frequent_flyer_level = ?"
                        + " WHERE id = ?",
                passenger.getFirstName(), passenger.getLastName(), passenger.getPassportNumber(),
                passenger.getEmail(), passenger.getPhone(), passenger.getDateOfBirth(),
                passenger.getFrequentFlyerLevel(), passenger.getId());
        if (rows == 0) {
            throw new DataAccessException("Пассажир с id=" + passenger.getId() + " не найден");
        }
    }

    @Override
    public void delete(long id) throws DataAccessException {
        update("DELETE FROM passengers WHERE id = ?", id);
    }

    @Override
    public long countBookings(long passengerId) throws DataAccessException {
        return queryLong("SELECT COUNT(*) FROM bookings WHERE passenger_id = ?", passengerId);
    }

    @Override
    public long count() throws DataAccessException {
        return queryLong("SELECT COUNT(*) FROM passengers");
    }

    private Passenger map(java.sql.ResultSet resultSet) throws java.sql.SQLException {
        Passenger passenger = new Passenger();
        passenger.setId(resultSet.getLong("id"));
        passenger.setFirstName(resultSet.getString("first_name"));
        passenger.setLastName(resultSet.getString("last_name"));
        passenger.setPassportNumber(resultSet.getString("passport_number"));
        passenger.setEmail(resultSet.getString("email"));
        passenger.setPhone(resultSet.getString("phone"));
        passenger.setDateOfBirth(getDate(resultSet, "date_of_birth"));
        passenger.setFrequentFlyerLevel(resultSet.getString("frequent_flyer_level"));
        passenger.setCreatedAt(getDateTime(resultSet, "created_at"));
        return passenger;
    }
}
