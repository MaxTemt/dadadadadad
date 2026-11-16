package com.airport.server.service;

import com.airport.common.exception.DataAccessException;
import com.airport.common.exception.NotFoundException;
import com.airport.common.exception.ValidationException;
import com.airport.common.model.Airline;
import com.airport.common.util.ValidationUtil;
import com.airport.server.dao.AirlineDao;

import java.util.List;

/**
 * Справочник авиакомпаний.
 */
public class AirlineService {

    private final AirlineDao airlineDao;

    public AirlineService(AirlineDao airlineDao) {
        this.airlineDao = airlineDao;
    }

    public List<Airline> list() throws DataAccessException {
        return airlineDao.findAll();
    }

    public Airline save(Airline airline) throws DataAccessException, ValidationException, NotFoundException {
        if (airline == null) {
            throw new ValidationException("Не переданы данные авиакомпании");
        }
        String name = ValidationUtil.requireText(airline.getName(), "Название авиакомпании", 100);
        String iata = ValidationUtil.requireIataCode(airline.getIataCode());
        String country = ValidationUtil.requireText(airline.getCountry(), "Страна", 60);
        String phone = ValidationUtil.optionalPhone(airline.getContactPhone(), "Телефон");

        var duplicate = airlineDao.findByIataCode(iata);
        if (duplicate.isPresent()
                && (airline.getId() == null || !duplicate.get().getId().equals(airline.getId()))) {
            throw new ValidationException("Авиакомпания с кодом IATA «" + iata + "» уже существует");
        }

        airline.setName(name);
        airline.setIataCode(iata);
        airline.setCountry(country);
        airline.setContactPhone(phone);

        if (airline.getId() == null) {
            airline.setId(airlineDao.insert(airline));
        } else {
            airlineDao.findById(airline.getId())
                    .orElseThrow(() -> new NotFoundException("Авиакомпания не найдена"));
            airlineDao.update(airline);
        }
        return airline;
    }

    public void delete(long id) throws DataAccessException, ValidationException, NotFoundException {
        airlineDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Авиакомпания не найдена"));
        if (airlineDao.countFlights(id) > 0) {
            throw new ValidationException("Нельзя удалить авиакомпанию: у неё есть рейсы");
        }
        airlineDao.delete(id);
    }
}
