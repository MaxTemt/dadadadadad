package com.airport.common.model;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Авиарейс.
 */
public class Flight extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    private String flightNumber;
    private Long airlineId;
    private String originCity;
    private String destinationCity;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String gate;
    private String aircraftType;
    private int totalSeats;
    private FlightStatus status = FlightStatus.SCHEDULED;
    private BigDecimal basePrice = BigDecimal.ZERO;

    /** Поля только для отображения (заполняются JOIN-запросом). */
    private String airlineName;
    private int bookedSeats;

    public Flight() {
    }

    public Flight(String flightNumber, Long airlineId, String originCity, String destinationCity,
                  LocalDateTime departureTime, LocalDateTime arrivalTime, int totalSeats,
                  BigDecimal basePrice) {
        this.flightNumber = flightNumber;
        this.airlineId = airlineId;
        this.originCity = originCity;
        this.destinationCity = destinationCity;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.totalSeats = totalSeats;
        this.basePrice = basePrice;
    }

    public Flight(String flightNumber, Long airlineId, String originCity, String destinationCity,
                  LocalDateTime departureTime, LocalDateTime arrivalTime, String gate,
                  String aircraftType, int totalSeats, FlightStatus status, BigDecimal basePrice) {
        this(flightNumber, airlineId, originCity, destinationCity, departureTime, arrivalTime,
                totalSeats, basePrice);
        this.gate = gate;
        this.aircraftType = aircraftType;
        this.status = status;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public Long getAirlineId() {
        return airlineId;
    }

    public void setAirlineId(Long airlineId) {
        this.airlineId = airlineId;
    }

    public String getOriginCity() {
        return originCity;
    }

    public void setOriginCity(String originCity) {
        this.originCity = originCity;
    }

    public String getDestinationCity() {
        return destinationCity;
    }

    public void setDestinationCity(String destinationCity) {
        this.destinationCity = destinationCity;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getGate() {
        return gate;
    }

    public void setGate(String gate) {
        this.gate = gate;
    }

    public String getAircraftType() {
        return aircraftType;
    }

    public void setAircraftType(String aircraftType) {
        this.aircraftType = aircraftType;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public FlightStatus getStatus() {
        return status;
    }

    public void setStatus(FlightStatus status) {
        this.status = status;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public String getAirlineName() {
        return airlineName;
    }

    public void setAirlineName(String airlineName) {
        this.airlineName = airlineName;
    }

    public int getBookedSeats() {
        return bookedSeats;
    }

    public void setBookedSeats(int bookedSeats) {
        this.bookedSeats = bookedSeats;
    }

    public int getAvailableSeats() {
        return Math.max(0, totalSeats - bookedSeats);
    }

    public double getOccupancyPercent() {
        if (totalSeats <= 0) {
            return 0;
        }
        return Math.min(100.0, bookedSeats * 100.0 / totalSeats);
    }

    public long getDurationMinutes() {
        if (departureTime == null || arrivalTime == null) {
            return 0;
        }
        return Duration.between(departureTime, arrivalTime).toMinutes();
    }

    @Override
    public String toString() {
        return flightNumber + " " + originCity + " → " + destinationCity;
    }
}
