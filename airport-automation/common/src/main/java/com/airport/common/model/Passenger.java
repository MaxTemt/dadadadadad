package com.airport.common.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Пассажир аэропорта.
 */
public class Passenger extends Person {

    private static final long serialVersionUID = 1L;

    private String passportNumber;
    private LocalDate dateOfBirth;
    private String frequentFlyerLevel = "NONE";
    private LocalDateTime createdAt;

    public Passenger() {
    }

    public Passenger(String firstName, String lastName, String passportNumber) {
        super(firstName, lastName);
        this.passportNumber = passportNumber;
    }

    public Passenger(String firstName, String lastName, String passportNumber,
                     String email, String phone, LocalDate dateOfBirth) {
        super(firstName, lastName, email, phone);
        this.passportNumber = passportNumber;
        this.dateOfBirth = dateOfBirth;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getFrequentFlyerLevel() {
        return frequentFlyerLevel;
    }

    public void setFrequentFlyerLevel(String frequentFlyerLevel) {
        this.frequentFlyerLevel = frequentFlyerLevel;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String getPersonType() {
        return "Пассажир";
    }

    @Override
    public String toString() {
        return "Passenger{id=" + getId() + ", passport='" + passportNumber + "', name='"
                + getFullName() + "'}";
    }
}
