package com.airport.common.dto;

import java.io.Serializable;

/**
 * Сводные показатели для панели аналитики.
 */
public class DashboardStats implements Serializable {

    private static final long serialVersionUID = 1L;

    private int totalFlights;
    private int todayFlights;
    private int totalPassengers;
    private int activeBookings;
    private double revenue;
    private double averageOccupancy;
    private int cancelledFlights;

    public DashboardStats() {
    }

    public DashboardStats(int totalFlights, int todayFlights, int totalPassengers,
                          int activeBookings, double revenue, double averageOccupancy,
                          int cancelledFlights) {
        this.totalFlights = totalFlights;
        this.todayFlights = todayFlights;
        this.totalPassengers = totalPassengers;
        this.activeBookings = activeBookings;
        this.revenue = revenue;
        this.averageOccupancy = averageOccupancy;
        this.cancelledFlights = cancelledFlights;
    }

    public int getTotalFlights() {
        return totalFlights;
    }

    public void setTotalFlights(int totalFlights) {
        this.totalFlights = totalFlights;
    }

    public int getTodayFlights() {
        return todayFlights;
    }

    public void setTodayFlights(int todayFlights) {
        this.todayFlights = todayFlights;
    }

    public int getTotalPassengers() {
        return totalPassengers;
    }

    public void setTotalPassengers(int totalPassengers) {
        this.totalPassengers = totalPassengers;
    }

    public int getActiveBookings() {
        return activeBookings;
    }

    public void setActiveBookings(int activeBookings) {
        this.activeBookings = activeBookings;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    public double getAverageOccupancy() {
        return averageOccupancy;
    }

    public void setAverageOccupancy(double averageOccupancy) {
        this.averageOccupancy = averageOccupancy;
    }

    public int getCancelledFlights() {
        return cancelledFlights;
    }

    public void setCancelledFlights(int cancelledFlights) {
        this.cancelledFlights = cancelledFlights;
    }
}
