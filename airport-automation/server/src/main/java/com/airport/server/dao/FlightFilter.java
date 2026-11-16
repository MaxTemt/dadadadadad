package com.airport.server.dao;

import com.airport.common.model.FlightStatus;

import java.time.LocalDate;

/**
 * Параметры поиска рейсов.
 */
public class FlightFilter {

    private String search;
    private FlightStatus status;
    private LocalDate date;

    public FlightFilter() {
    }

    public FlightFilter(String search) {
        this.search = search;
    }

    public FlightFilter(String search, FlightStatus status, LocalDate date) {
        this.search = search;
        this.status = status;
        this.date = date;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public FlightStatus getStatus() {
        return status;
    }

    public void setStatus(FlightStatus status) {
        this.status = status;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
