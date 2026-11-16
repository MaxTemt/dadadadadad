package com.airport.common.model;

/**
 * Абстрактный человек — общая часть сотрудника и пассажира.
 */
public abstract class Person extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    protected Person() {
    }

    protected Person(String firstName, String lastName) {
        this(firstName, lastName, null, null);
    }

    protected Person(String firstName, String lastName, String email, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFullName() {
        String first = firstName == null ? "" : firstName.trim();
        String last = lastName == null ? "" : lastName.trim();
        return (last + " " + first).trim();
    }

    /** Вид человека: переопределяется наследниками. */
    public abstract String getPersonType();
}
