package com.airport.server.service;

import com.airport.common.exception.DataAccessException;
import com.airport.common.exception.NotFoundException;
import com.airport.common.exception.ValidationException;
import com.airport.common.model.Flight;
import com.airport.common.model.FlightStatus;
import com.airport.common.util.ValidationUtil;
import com.airport.server.dao.AirlineDao;
import com.airport.server.dao.BookingDao;
import com.airport.server.dao.FlightDao;
import com.airport.server.dao.FlightFilter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Бизнес-логика работы с рейсами.
 */
public class FlightService {

    private final FlightDao flightDao;
    private final AirlineDao airlineDao;
    private final BookingDao bookingDao;

    public FlightService(FlightDao flightDao, AirlineDao airlineDao, BookingDao bookingDao) {
        this.flightDao = flightDao;
        this.airlineDao = airlineDao;
        this.bookingDao = bookingDao;
    }

    public List<Flight> search(FlightFilter filter) throws DataAccessException {
        return flightDao.search(filter);
    }

    public Flight get(long id) throws DataAccessException, NotFoundException {
        return flightDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Рейс не найден"));
    }

    public Flight save(Flight flight) throws DataAccessException, ValidationException, NotFoundException {
        if (flight == null) {
            throw new ValidationException("Не переданы данные рейса");
        }
        String number = ValidationUtil.requireFlightNumber(flight.getFlightNumber());
        ValidationUtil.requireId(flight.getAirlineId(), "Авиакомпания");
        airlineDao.findById(flight.getAirlineId())
                .orElseThrow(() -> new ValidationException("Выбранная авиакомпания не найдена"));

        String origin = ValidationUtil.requireText(flight.getOriginCity(), "Город вылета", 80);
        String destination = ValidationUtil.requireText(flight.getDestinationCity(), "Город прилёта", 80);
        ValidationUtil.requireNotSame(origin, destination, "Города вылета и прилёта должны различаться");
        ValidationUtil.requireDateTimeRange(flight.getDepartureTime(), flight.getArrivalTime());
        ValidationUtil.requireRange(flight.getTotalSeats(), 1, 1000, "Количество мест");
        ValidationUtil.requirePositive(flight.getBasePrice(), "Базовая цена");
        String gate = ValidationUtil.optionalText(flight.getGate(), "Выход на посадку", 10);
        String aircraft = ValidationUtil.optionalText(flight.getAircraftType(), "Тип воздушного судна", 50);
        FlightStatus status = flight.getStatus() == null ? FlightStatus.SCHEDULED : flight.getStatus();

        var duplicate = flightDao.findByNumber(number);
        if (duplicate.isPresent()
                && (flight.getId() == null || !duplicate.get().getId().equals(flight.getId()))) {
            throw new ValidationException("Рейс с номером «" + number + "» уже существует");
        }

        flight.setFlightNumber(number);
        flight.setOriginCity(origin);
        flight.setDestinationCity(destination);
        flight.setGate(gate);
        flight.setAircraftType(aircraft);
        flight.setStatus(status);
        flight.setBasePrice(flight.getBasePrice().setScale(2, RoundingMode.HALF_UP));

        if (flight.getId() == null) {
            flight.setId(flightDao.insert(flight));
        } else {
            flightDao.findById(flight.getId())
                    .orElseThrow(() -> new NotFoundException("Рейс не найден"));
            long booked = bookingDao.countActiveByFlight(flight.getId());
            if (flight.getTotalSeats() < booked) {
                throw new ValidationException("Нельзя уменьшить число мест ниже количества "
                        + "активных бронирований (" + booked + ")");
            }
            flightDao.update(flight);
        }
        return flight;
    }

    public void delete(long id) throws DataAccessException, ValidationException, NotFoundException {
        flightDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Рейс не найден"));
        if (flightDao.countBookings(id) > 0) {
            throw new ValidationException("Нельзя удалить рейс: сначала удалите его бронирования");
        }
        flightDao.delete(id);
    }

    public BigDecimal calculatePrice(Flight flight, BigDecimal multiplier) {
        return flight.getBasePrice().multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }
}
