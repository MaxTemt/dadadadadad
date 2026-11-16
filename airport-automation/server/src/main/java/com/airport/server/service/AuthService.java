package com.airport.server.service;

import com.airport.common.exception.AuthenticationException;
import com.airport.common.exception.AuthorizationException;
import com.airport.common.exception.DataAccessException;
import com.airport.common.exception.NotFoundException;
import com.airport.common.exception.ValidationException;
import com.airport.common.model.Role;
import com.airport.common.model.User;
import com.airport.common.util.PasswordUtil;
import com.airport.common.util.ValidationUtil;
import com.airport.server.dao.UserDao;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Аутентификация и управление учётными записями.
 */
public class AuthService {

    private final UserDao userDao;

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User login(String username, String password) throws AuthenticationException, ValidationException,
            DataAccessException {
        String login = ValidationUtil.requireText(username, "Логин", 30);
        if (password == null || password.isEmpty()) {
            throw new ValidationException("Введите пароль");
        }
        User user = userDao.findByUsername(login)
                .orElseThrow(() -> new AuthenticationException("Неверный логин или пароль"));
        if (!user.isActive()) {
            throw new AuthenticationException("Учётная запись заблокирована");
        }
        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            throw new AuthenticationException("Неверный логин или пароль");
        }
        return sanitize(user);
    }

    public List<User> listUsers() throws DataAccessException {
        List<User> users = userDao.findAll();
        users.forEach(this::sanitize);
        return users;
    }

    /**
     * Создание или изменение пользователя. Для нового пользователя пароль обязателен,
     * для существующего — пустое значение означает «оставить прежний».
     */
    public User saveUser(User user, String rawPassword) throws DataAccessException, ValidationException,
            NotFoundException, AuthorizationException {
        if (user == null) {
            throw new ValidationException("Не переданы данные пользователя");
        }
        String username = ValidationUtil.requireUsername(user.getUsername());
        String firstName = ValidationUtil.requireText(user.getFirstName(), "Имя", 50);
        String lastName = ValidationUtil.requireText(user.getLastName(), "Фамилия", 50);
        String email = ValidationUtil.optionalEmail(user.getEmail(), "E-mail");
        String phone = ValidationUtil.optionalPhone(user.getPhone(), "Телефон");
        Role role = user.getRole() == null ? Role.USER : user.getRole();

        var duplicate = userDao.findByUsername(username);
        if (duplicate.isPresent()
                && (user.getId() == null || !duplicate.get().getId().equals(user.getId()))) {
            throw new ValidationException("Пользователь с логином «" + username + "» уже существует");
        }

        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(role);

        if (user.getId() == null) {
            ValidationUtil.requirePassword(rawPassword);
            user.setPasswordHash(PasswordUtil.hashPassword(rawPassword));
            user.setCreatedAt(LocalDateTime.now());
            long id = userDao.insert(user);
            user.setId(id);
        } else {
            User existing = userDao.findById(user.getId())
                    .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
            boolean losesAdmin = existing.isAdmin() && existing.isActive()
                    && (role != Role.ADMIN || !user.isActive());
            if (losesAdmin && userDao.countAdmins() <= 1) {
                throw new AuthorizationException("Нельзя лишить прав последнего активного администратора");
            }
            if (rawPassword != null && !rawPassword.isBlank()) {
                ValidationUtil.requirePassword(rawPassword);
                user.setPasswordHash(PasswordUtil.hashPassword(rawPassword));
            } else {
                user.setPasswordHash(existing.getPasswordHash());
            }
            user.setCreatedAt(existing.getCreatedAt());
            userDao.update(user);
        }
        return sanitize(user);
    }

    public void deleteUser(long id, long currentUserId) throws DataAccessException, ValidationException,
            NotFoundException, AuthorizationException {
        if (id == currentUserId) {
            throw new AuthorizationException("Нельзя удалить собственную учётную запись");
        }
        User existing = userDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        if (existing.isAdmin() && userDao.countAdmins() <= 1) {
            throw new AuthorizationException("Нельзя удалить последнего администратора");
        }
        if (userDao.countCreatedBookings(id) > 0) {
            throw new ValidationException("Нельзя удалить пользователя: за ним закреплены бронирования");
        }
        userDao.delete(id);
    }

    private User sanitize(User user) {
        user.setPasswordHash(null);
        return user;
    }
}
