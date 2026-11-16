package com.airport.common.model;

/**
 * Авиакомпания.
 */
public class Airline extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    private String name;
    private String iataCode;
    private String country;
    private String contactPhone;

    public Airline() {
    }

    public Airline(String name, String iataCode, String country) {
        this(name, iataCode, country, null);
    }

    public Airline(String name, String iataCode, String country, String contactPhone) {
        this.name = name;
        this.iataCode = iataCode;
        this.country = country;
        this.contactPhone = contactPhone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIataCode() {
        return iataCode;
    }

    public void setIataCode(String iataCode) {
        this.iataCode = iataCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    @Override
    public String toString() {
        return name + " (" + iataCode + ")";
    }
}
