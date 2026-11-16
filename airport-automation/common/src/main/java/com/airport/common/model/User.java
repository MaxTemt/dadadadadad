package com.airport.common.model;

import java.time.LocalDateTime;

/**
 * Учётная запись сотрудника аэропорта.
 */
public class User extends Person {

    private static final long serialVersionUID = 1L;

    private String username;
    private String passwordHash;
    private Role role = Role.USER;
    private boolean active = true;
    private LocalDateTime createdAt;

    public User() {
    }

    public User(String username, String passwordHash, Role role, String firstName, String lastName) {
        super(firstName, lastName);
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public User(String username, String passwordHash, Role role, String firstName, String lastName,
                String email, String phone, boolean active) {
        super(firstName, lastName, email, phone);
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = active;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    @Override
    public String getPersonType() {
        return "Сотрудник";
    }

    @Override
    public String toString() {
        return "User{id=" + getId() + ", username='" + username + "', role=" + role + '}';
    }
}
