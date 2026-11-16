package com.airport.server.dao;

import com.airport.common.exception.DataAccessException;
import com.airport.common.model.Airline;
import com.airport.server.db.ConnectionPool;

import java.util.List;
import java.util.Optional;

public class JdbcAirlineDao extends AbstractJdbcDao implements AirlineDao {

    private static final String COLUMNS = "id, name, iata_code, country, contact_phone";

    public JdbcAirlineDao(ConnectionPool pool) {
        super(pool);
    }

    @Override
    public List<Airline> findAll() throws DataAccessException {
        return queryList("SELECT " + COLUMNS + " FROM airlines ORDER BY name", this::map);
    }

    @Override
    public Optional<Airline> findById(long id) throws DataAccessException {
        return queryOne("SELECT " + COLUMNS + " FROM airlines WHERE id = ?", this::map, id);
    }

    @Override
    public Optional<Airline> findByIataCode(String code) throws DataAccessException {
        return queryOne("SELECT " + COLUMNS + " FROM airlines WHERE UPPER(iata_code) = UPPER(?)",
                this::map, code);
    }

    @Override
    public long insert(Airline airline) throws DataAccessException {
        return insert("INSERT INTO airlines (name, iata_code, country, contact_phone)"
                        + " VALUES (?, ?, ?, ?)",
                airline.getName(), airline.getIataCode(), airline.getCountry(),
                airline.getContactPhone());
    }

    @Override
    public void update(Airline airline) throws DataAccessException {
        int rows = update("UPDATE airlines SET name = ?, iata_code = ?, country = ?,"
                        + " contact_phone = ? WHERE id = ?",
                airline.getName(), airline.getIataCode(), airline.getCountry(),
                airline.getContactPhone(), airline.getId());
        if (rows == 0) {
            throw new DataAccessException("Авиакомпания с id=" + airline.getId() + " не найдена");
        }
    }

    @Override
    public void delete(long id) throws DataAccessException {
        update("DELETE FROM airlines WHERE id = ?", id);
    }

    @Override
    public long countFlights(long airlineId) throws DataAccessException {
        return queryLong("SELECT COUNT(*) FROM flights WHERE airline_id = ?", airlineId);
    }

    private Airline map(java.sql.ResultSet resultSet) throws java.sql.SQLException {
        Airline airline = new Airline();
        airline.setId(resultSet.getLong("id"));
        airline.setName(resultSet.getString("name"));
        airline.setIataCode(resultSet.getString("iata_code"));
        airline.setCountry(resultSet.getString("country"));
        airline.setContactPhone(resultSet.getString("contact_phone"));
        return airline;
    }
}
