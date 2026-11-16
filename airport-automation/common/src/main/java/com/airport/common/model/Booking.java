package com.airport.common.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Бронирование (билет) пассажира на рейс.
 */
public class Booking extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    private Long flightId;
    private Long passengerId;
    private String seatNumber;
    private TicketClass ticketClass = TicketClass.ECONOMY;
    private BigDecimal price = BigDecimal.ZERO;
    private LocalDateTime bookingTime;
    private BookingStatus status = BookingStatus.CONFIRMED;
    private Long createdBy;

    /** Поля только для отображения (заполняются JOIN-запросом). */
    private String flightNumber;
    private String passengerName;
    private String destinationCity;
    private String airlineName;

    public Booking() {
    }

    public Booking(Long flightId, Long passengerId, String seatNumber, TicketClass ticketClass) {
        this.flightId = flightId;
        this.passengerId = passengerId;
        this.seatNumber = seatNumber;
        this.ticketClass = ticketClass;
    }

    public Booking(Long flightId, Long passengerId, String seatNumber, TicketClass ticketClass,
                   BigDecimal price, BookingStatus status) {
        this(flightId, passengerId, seatNumber, ticketClass);
        this.price = price;
        this.status = status;
    }

    public Long getFlightId() {
        return flightId;
    }

    public void setFlightId(Long flightId) {
        this.flightId = flightId;
    }

    public Long getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(Long passengerId) {
        this.passengerId = passengerId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public TicketClass getTicketClass() {
        return ticketClass;
    }

    public void setTicketClass(TicketClass ticketClass) {
        this.ticketClass = ticketClass;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getDestinationCity() {
        return destinationCity;
    }

    public void setDestinationCity(String destinationCity) {
        this.destinationCity = destinationCity;
    }

    public String getAirlineName() {
        return airlineName;
    }

    public void setAirlineName(String airlineName) {
        this.airlineName = airlineName;
    }

    @Override
    public String toString() {
        return "Билет " + seatNumber + " (" + flightNumber + ", " + passengerName + ")";
    }
}
