package com.airport.server.service;

import com.airport.common.exception.DataAccessException;
import com.airport.common.exception.NotFoundException;
import com.airport.common.exception.ValidationException;
import com.airport.common.model.Passenger;
import com.airport.common.util.ValidationUtil;
import com.airport.server.dao.PassengerDao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Работа с пассажирами.
 */
public class PassengerService {

    private static final Set<String> LEVELS = Set.of("NONE", "SILVER", "GOLD", "PLATINUM");

    private final PassengerDao passengerDao;

    public PassengerService(PassengerDao passengerDao) {
        this.passengerDao = passengerDao;
    }

    public List<Passenger> search(String search) throws DataAccessException {
        return passengerDao.search(search);
    }

    public Passenger get(long id) throws DataAccessException, NotFoundException {
        return passengerDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Пассажир не найден"));
    }

    public Passenger save(Passenger passenger) throws DataAccessException, ValidationException,
            NotFoundException {
        if (passenger == null) {
            throw new ValidationException("Не переданы данные пассажира");
        }
        String firstName = ValidationUtil.requireText(passenger.getFirstName(), "Имя", 50);
        String lastName = ValidationUtil.requireText(passenger.getLastName(), "Фамилия", 50);
        String passport = ValidationUtil.requirePassport(passenger.getPassportNumber());
        String email = ValidationUtil.optionalEmail(passenger.getEmail(), "E-mail");
        String phone = ValidationUtil.optionalPhone(passenger.getPhone(), "Телефон");
        ValidationUtil.requireDateOfBirth(passenger.getDateOfBirth());

        String level = passenger.getFrequentFlyerLevel() == null
                ? "NONE" : passenger.getFrequentFlyerLevel().toUpperCase();
        if (!LEVELS.contains(level)) {
            throw new ValidationException("Некорректный уровень часто летающего пассажира");
        }

        var duplicate = passengerDao.findByPassport(passport);
        if (duplicate.isPresent()
                && (passenger.getId() == null || !duplicate.get().getId().equals(passenger.getId()))) {
            throw new ValidationException("Пассажир с паспортом «" + passport + "» уже существует");
        }

        passenger.setFirstName(firstName);
        passenger.setLastName(lastName);
        passenger.setPassportNumber(passport);
        passenger.setEmail(email);
        passenger.setPhone(phone);
        passenger.setFrequentFlyerLevel(level);

        if (passenger.getId() == null) {
            passenger.setCreatedAt(LocalDateTime.now());
            passenger.setId(passengerDao.insert(passenger));
        } else {
            passengerDao.findById(passenger.getId())
                    .orElseThrow(() -> new NotFoundException("Пассажир не найден"));
            passengerDao.update(passenger);
        }
        return passenger;
    }

    public void delete(long id) throws DataAccessException, ValidationException, NotFoundException {
        passengerDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Пассажир не найден"));
        if (passengerDao.countBookings(id) > 0) {
            throw new ValidationException("Нельзя удалить пассажира: у него есть бронирования");
        }
        passengerDao.delete(id);
    }
}
