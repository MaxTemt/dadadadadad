package com.airport.server.dao;

import com.airport.common.model.BookingStatus;

/**
 * Параметры поиска бронирований.
 */
public class BookingFilter {

    private String search;
    private Long flightId;
    private Long passengerId;
    private BookingStatus status;

    public BookingFilter() {
    }

    public BookingFilter(String search) {
        this.search = search;
    }

    public BookingFilter(Long flightId, Long passengerId, BookingStatus status, String search) {
        this.flightId = flightId;
        this.passengerId = passengerId;
        this.status = status;
        this.search = search;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
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

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}
