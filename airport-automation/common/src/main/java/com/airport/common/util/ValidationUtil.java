package com.airport.common.util;

import com.airport.common.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.regex.Pattern;

/**
 * Статические методы проверки данных, используемые на сервере и на клиенте.
 */
public final class ValidationUtil {

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^[+]?[0-9 ()-]{7,25}$");
    private static final Pattern FLIGHT_NUMBER = Pattern.compile("^[A-Z0-9]{2,10}$");
    private static final Pattern IATA = Pattern.compile("^[A-Z0-9]{2}$");
    private static final Pattern SEAT = Pattern.compile("^[0-9]{1,3}[A-F]$");
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_.-]{3,30}$");
    private static final Pattern PASSPORT = Pattern.compile("^[A-Z0-9 -]{5,30}$");

    private ValidationUtil() {
    }

    public static String requireText(String value, String field, int maxLength) throws ValidationException {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Поле «" + field + "» обязательно для заполнения");
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new ValidationException("Поле «" + field + "» не должно превышать "
                    + maxLength + " символов");
        }
        return trimmed;
    }

    public static String optionalText(String value, String field, int maxLength) throws ValidationException {
        if (value == null || value.isBlank()) {
            return null;
        }
        return requireText(value, field, maxLength);
    }

    public static String requireEmail(String value, String field) throws ValidationException {
        String email = requireText(value, field, 120);
        if (!EMAIL.matcher(email).matches()) {
            throw new ValidationException("Поле «" + field + "» содержит некорректный e-mail");
        }
        return email;
    }

    public static String optionalEmail(String value, String field) throws ValidationException {
        if (value == null || value.isBlank()) {
            return null;
        }
        return requireEmail(value, field);
    }

    public static String optionalPhone(String value, String field) throws ValidationException {
        if (value == null || value.isBlank()) {
            return null;
        }
        String phone = requireText(value, field, 25);
        if (!PHONE.matcher(phone).matches()) {
            throw new ValidationException("Поле «" + field + "» содержит некорректный телефон");
        }
        return phone;
    }

    public static String requireFlightNumber(String value) throws ValidationException {
        String number = requireText(value, "Номер рейса", 10).toUpperCase();
        if (!FLIGHT_NUMBER.matcher(number).matches()) {
            throw new ValidationException("Номер рейса должен состоять из 2–10 латинских букв и цифр");
        }
        return number;
    }

    public static String requireIataCode(String value) throws ValidationException {
        String code = requireText(value, "Код IATA", 2).toUpperCase();
        if (!IATA.matcher(code).matches()) {
            throw new ValidationException("Код IATA должен состоять из двух латинских букв или цифр");
        }
        return code;
    }

    public static String requireSeatNumber(String value) throws ValidationException {
        String seat = requireText(value, "Номер места", 4).toUpperCase();
        if (!SEAT.matcher(seat).matches()) {
            throw new ValidationException("Номер места должен иметь вид 12A (ряд и буква A–F)");
        }
        return seat;
    }

    public static String requireUsername(String value) throws ValidationException {
        String username = requireText(value, "Логин", 30);
        if (!USERNAME.matcher(username).matches()) {
            throw new ValidationException("Логин должен содержать 3–30 символов: латинские буквы, цифры, _ . -");
        }
        return username;
    }

    public static String requirePassword(String value) throws ValidationException {
        if (value == null || value.length() < 6) {
            throw new ValidationException("Пароль должен содержать не менее 6 символов");
        }
        if (value.length() > 100) {
            throw new ValidationException("Пароль слишком длинный");
        }
        return value;
    }

    public static String requirePassport(String value) throws ValidationException {
        String passport = requireText(value, "Номер паспорта", 30).toUpperCase();
        if (!PASSPORT.matcher(passport).matches()) {
            throw new ValidationException("Некорректный номер паспорта");
        }
        return passport;
    }

    public static void requireDateOfBirth(LocalDate date) throws ValidationException {
        if (date == null) {
            throw new ValidationException("Дата рождения обязательна");
        }
        LocalDate today = LocalDate.now();
        if (date.isAfter(today)) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        if (Period.between(date, today).getYears() > 120) {
            throw new ValidationException("Некорректная дата рождения");
        }
    }

    public static void requireDateTimeRange(LocalDateTime start, LocalDateTime end)
            throws ValidationException {
        if (start == null || end == null) {
            throw new ValidationException("Дата и время вылета и прилёта обязательны");
        }
        if (!end.isAfter(start)) {
            throw new ValidationException("Время прилёта должно быть позже времени вылета");
        }
    }

    public static void requirePositive(BigDecimal value, String field) throws ValidationException {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Поле «" + field + "» должно быть положительным числом");
        }
    }

    public static void requireRange(int value, int min, int max, String field) throws ValidationException {
        if (value < min || value > max) {
            throw new ValidationException("Поле «" + field + "» должно быть в диапазоне от "
                    + min + " до " + max);
        }
    }

    public static void requireId(Long id, String field) throws ValidationException {
        if (id == null || id <= 0) {
            throw new ValidationException("Не указан идентификатор: " + field);
        }
    }

    public static void requireNotSame(String first, String second, String message)
            throws ValidationException {
        if (first != null && second != null && first.equalsIgnoreCase(second)) {
            throw new ValidationException(message);
        }
    }
}
