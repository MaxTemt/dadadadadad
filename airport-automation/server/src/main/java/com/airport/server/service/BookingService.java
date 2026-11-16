package com.airport.server.service;

import com.airport.common.exception.DataAccessException;
import com.airport.common.exception.NotFoundException;
import com.airport.common.exception.ValidationException;
import com.airport.common.model.Booking;
import com.airport.common.model.BookingStatus;
import com.airport.common.model.Flight;
import com.airport.common.model.FlightStatus;
import com.airport.common.model.TicketClass;
import com.airport.common.util.ValidationUtil;
import com.airport.server.dao.BookingDao;
import com.airport.server.dao.BookingFilter;
import com.airport.server.dao.FlightDao;
import com.airport.server.dao.PassengerDao;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Бизнес-логика бронирования билетов: проверка мест, расчёт стоимости,
 * регистрация и отмена брони.
 */
public class BookingService {

    private final BookingDao bookingDao;
    private final FlightDao flightDao;
    private final PassengerDao passengerDao;

    public BookingService(BookingDao bookingDao, FlightDao flightDao, PassengerDao passengerDao) {
        this.bookingDao = bookingDao;
        this.flightDao = flightDao;
        this.passengerDao = passengerDao;
    }

    public List<Booking> search(BookingFilter filter) throws DataAccessException {
        return bookingDao.search(filter);
    }

    public Booking get(long id) throws DataAccessException, NotFoundException {
        return bookingDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));
    }

    public Booking save(Booking booking, long actorId) throws DataAccessException, ValidationException,
            NotFoundException {
        if (booking == null) {
            throw new ValidationException("Не переданы данные бронирования");
        }
        ValidationUtil.requireId(booking.getFlightId(), "Рейс");
        ValidationUtil.requireId(booking.getPassengerId(), "Пассажир");
        Flight flight = flightDao.findById(booking.getFlightId())
                .orElseThrow(() -> new ValidationException("Выбранный рейс не найден"));
        passengerDao.findById(booking.getPassengerId())
                .orElseThrow(() -> new ValidationException("Выбранный пассажир не найден"));

        if (flight.getStatus() == FlightStatus.CANCELLED) {
            throw new ValidationException("Нельзя оформить билет на отменённый рейс");
        }
        if (flight.getDepartureTime() != null && flight.getDepartureTime().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Рейс уже вылетел — бронирование невозможно");
        }

        String seat = ValidationUtil.requireSeatNumber(booking.getSeatNumber());
        TicketClass ticketClass = booking.getTicketClass() == null
                ? TicketClass.ECONOMY : booking.getTicketClass();

        Booking existing = null;
        if (booking.getId() != null) {
            existing = bookingDao.findById(booking.getId())
                    .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));
            booking.setBookingTime(existing.getBookingTime());
            booking.setCreatedBy(existing.getCreatedBy());
        }
        BookingStatus status = booking.getStatus() == null
                ? (existing == null ? BookingStatus.CONFIRMED : existing.getStatus())
                : booking.getStatus();

        if (bookingDao.existsSeat(flight.getId(), seat, booking.getId())) {
            throw new ValidationException("Место " + seat + " уже занято на этом рейсе");
        }

        long active = bookingDao.countActiveByFlight(flight.getId());
        if (existing != null && existing.getStatus() != BookingStatus.CANCELLED
                && existing.getFlightId().equals(flight.getId())) {
            active--;
        }
        if (status != BookingStatus.CANCELLED && active >= flight.getTotalSeats()) {
            throw new ValidationException("На рейсе нет свободных мест");
        }
        if (status == BookingStatus.CHECKED_IN && flight.getStatus() != FlightStatus.BOARDING
                && flight.getStatus() != FlightStatus.SCHEDULED
                && flight.getStatus() != FlightStatus.DELAYED) {
            throw new ValidationException("Регистрация на этот рейс недоступна");
        }

        booking.setSeatNumber(seat);
        booking.setTicketClass(ticketClass);
        booking.setStatus(status);
        booking.setPrice(flight.getBasePrice()
                .multiply(ticketClass.getPriceMultiplier())
                .setScale(2, RoundingMode.HALF_UP));
        if (booking.getBookingTime() == null) {
            booking.setBookingTime(LocalDateTime.now());
        }
        if (booking.getCreatedBy() == null) {
            booking.setCreatedBy(actorId);
        }

        if (booking.getId() == null) {
            booking.setId(bookingDao.insert(booking));
        } else {
            bookingDao.update(booking);
        }
        return get(booking.getId());
    }

    public Booking checkIn(long id) throws DataAccessException, ValidationException, NotFoundException {
        Booking booking = get(id);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ValidationException("Нельзя зарегистрировать отменённое бронирование");
        }
        if (booking.getStatus() == BookingStatus.CHECKED_IN) {
            throw new ValidationException("Пассажир уже прошёл регистрацию");
        }
        Flight flight = flightDao.findById(booking.getFlightId())
                .orElseThrow(() -> new NotFoundException("Рейс не найден"));
        if (flight.getStatus() == FlightStatus.CANCELLED || flight.getStatus() == FlightStatus.DEPARTED) {
            throw new ValidationException("Регистрация на рейс закрыта");
        }
        booking.setStatus(BookingStatus.CHECKED_IN);
        bookingDao.update(booking);
        return get(id);
    }

    public Booking cancel(long id) throws DataAccessException, ValidationException, NotFoundException {
        Booking booking = get(id);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ValidationException("Бронирование уже отменено");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        bookingDao.update(booking);
        return get(id);
    }

    public void delete(long id) throws DataAccessException, NotFoundException {
        get(id);
        bookingDao.delete(id);
    }
}
