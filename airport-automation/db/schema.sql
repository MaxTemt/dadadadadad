-- ============================================================================
--  Система автоматизации работы аэропорта
--  Схема базы данных MySQL 8 (InnoDB, 3-я нормальная форма)
--
--  Запуск:  mysql -u root -p < db/schema.sql
-- ============================================================================

CREATE DATABASE IF NOT EXISTS airport
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE airport;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS flights;
DROP TABLE IF EXISTS passengers;
DROP TABLE IF EXISTS airlines;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------------
-- Сотрудники (учётные записи)
-- ---------------------------------------------------------------------------
CREATE TABLE users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    username      VARCHAR(30)  NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    user_role     ENUM('ADMIN', 'USER') NOT NULL DEFAULT 'USER',
    first_name    VARCHAR(50)  NOT NULL,
    last_name     VARCHAR(50)  NOT NULL,
    email         VARCHAR(100) NULL,
    phone         VARCHAR(30)  NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username),
    CONSTRAINT chk_users_email CHECK (email IS NULL OR email LIKE '%_@_%._%')
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- Авиакомпании
-- ---------------------------------------------------------------------------
CREATE TABLE airlines (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(100) NOT NULL,
    iata_code     VARCHAR(3)   NOT NULL,
    country       VARCHAR(80)  NOT NULL,
    contact_phone VARCHAR(30)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_airlines_name (name),
    UNIQUE KEY uk_airlines_iata (iata_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- Пассажиры
-- ---------------------------------------------------------------------------
CREATE TABLE passengers (
    id                    BIGINT      NOT NULL AUTO_INCREMENT,
    first_name            VARCHAR(50) NOT NULL,
    last_name             VARCHAR(50) NOT NULL,
    passport_number       VARCHAR(20) NOT NULL,
    email                 VARCHAR(100) NULL,
    phone                 VARCHAR(30) NULL,
    date_of_birth         DATE        NOT NULL,
    frequent_flyer_level  ENUM('NONE', 'SILVER', 'GOLD', 'PLATINUM') NOT NULL DEFAULT 'NONE',
    created_at            DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_passengers_passport (passport_number),
    KEY idx_passengers_name (last_name, first_name),
    CONSTRAINT chk_passengers_email CHECK (email IS NULL OR email LIKE '%_@_%._%')
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- Рейсы
-- ---------------------------------------------------------------------------
CREATE TABLE flights (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    flight_number    VARCHAR(10)   NOT NULL,
    airline_id       BIGINT        NOT NULL,
    origin_city      VARCHAR(80)   NOT NULL,
    destination_city VARCHAR(80)   NOT NULL,
    departure_time   DATETIME      NOT NULL,
    arrival_time     DATETIME      NOT NULL,
    gate_number      VARCHAR(10)   NULL,
    aircraft_type    VARCHAR(50)   NULL,
    total_seats      INT           NOT NULL,
    flight_status    ENUM('SCHEDULED', 'BOARDING', 'DEPARTED', 'DELAYED', 'CANCELLED')
                                   NOT NULL DEFAULT 'SCHEDULED',
    base_price       DECIMAL(12, 2) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_flights_number (flight_number),
    KEY idx_flights_airline (airline_id),
    KEY idx_flights_departure (departure_time),
    CONSTRAINT fk_flights_airline FOREIGN KEY (airline_id)
        REFERENCES airlines (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_flights_seats CHECK (total_seats BETWEEN 1 AND 1000),
    CONSTRAINT chk_flights_price CHECK (base_price > 0),
    CONSTRAINT chk_flights_time CHECK (arrival_time > departure_time),
    CONSTRAINT chk_flights_route CHECK (origin_city <> destination_city)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- Бронирования (билеты)
-- ---------------------------------------------------------------------------
CREATE TABLE bookings (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    flight_id      BIGINT        NOT NULL,
    passenger_id   BIGINT        NOT NULL,
    seat_number    VARCHAR(5)    NOT NULL,
    ticket_class   ENUM('ECONOMY', 'BUSINESS', 'FIRST') NOT NULL DEFAULT 'ECONOMY',
    price          DECIMAL(12, 2) NOT NULL,
    booking_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    booking_status ENUM('CONFIRMED', 'CHECKED_IN', 'CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
    created_by     BIGINT        NOT NULL,
    PRIMARY KEY (id),
    KEY idx_bookings_flight (flight_id),
    KEY idx_bookings_passenger (passenger_id),
    KEY idx_bookings_created_by (created_by),
    KEY idx_bookings_status (booking_status),
    CONSTRAINT fk_bookings_flight FOREIGN KEY (flight_id)
        REFERENCES flights (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_bookings_passenger FOREIGN KEY (passenger_id)
        REFERENCES passengers (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_bookings_user FOREIGN KEY (created_by)
        REFERENCES users (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_bookings_price CHECK (price > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ============================================================================
--  Демонстрационные данные
-- ============================================================================

-- Пароли: admin123 и agent123 (PBKDF2WithHmacSHA256, 120000 итераций)
INSERT INTO users (id, username, password_hash, user_role, first_name, last_name,
                   email, phone, active, created_at) VALUES
    (1, 'admin',
     'pbkdf2$120000$D5Vmv3yTwDnv7xZLD7k/lA==$Gyje29TupWWmnGEntVh2WCHIYHmoFBCyQEIeajqfjCo=',
     'ADMIN', 'Игорь', 'Смирнов', 'admin@airport.local', '+7 (495) 100-10-10', TRUE,
     DATE_SUB(NOW(), INTERVAL 90 DAY)),
    (2, 'agent',
     'pbkdf2$120000$x/gKwGIECehIiZQ0k2jgsQ==$gzmGaouz33a/srpqM4eMbo68VwWUxgnlkS99bAdmAms=',
     'USER', 'Анна', 'Кузнецова', 'agent@airport.local', '+7 (495) 100-20-20', TRUE,
     DATE_SUB(NOW(), INTERVAL 60 DAY));

INSERT INTO airlines (id, name, iata_code, country, contact_phone) VALUES
    (1, 'Аэрофлот', 'SU', 'Россия', '+7 (495) 223-55-55'),
    (2, 'Победа', 'DP', 'Россия', '+7 (809) 505-40-70'),
    (3, 'S7 Airlines', 'S7', 'Россия', '+7 (800) 700-07-07'),
    (4, 'Уральские авиалинии', 'U6', 'Россия', '+7 (800) 770-00-10'),
    (5, 'Nordwind Airlines', 'N4', 'Россия', '+7 (495) 730-50-50');

INSERT INTO passengers (id, first_name, last_name, passport_number, email, phone,
                        date_of_birth, frequent_flyer_level, created_at) VALUES
    (1, 'Пётр', 'Иванов', '4510 123456', 'petr.ivanov@example.com', '+7 (916) 111-22-33',
     '1985-04-12', 'GOLD', DATE_SUB(NOW(), INTERVAL 40 DAY)),
    (2, 'Мария', 'Петрова', '4511 654321', 'maria.petrova@example.com', '+7 (916) 222-33-44',
     '1990-09-23', 'PLATINUM', DATE_SUB(NOW(), INTERVAL 35 DAY)),
    (3, 'Алексей', 'Соколов', '4512 777888', 'alexey.sokolov@example.com', '+7 (916) 333-44-55',
     '1978-01-30', 'SILVER', DATE_SUB(NOW(), INTERVAL 20 DAY)),
    (4, 'Елена', 'Морозова', '4513 999000', 'elena.morozova@example.com', '+7 (916) 444-55-66',
     '1995-12-05', 'NONE', DATE_SUB(NOW(), INTERVAL 10 DAY)),
    (5, 'Дмитрий', 'Волков', '4514 121212', 'dmitry.volkov@example.com', '+7 (916) 555-66-77',
     '1982-06-18', 'GOLD', DATE_SUB(NOW(), INTERVAL 5 DAY));

INSERT INTO flights (id, flight_number, airline_id, origin_city, destination_city,
                     departure_time, arrival_time, gate_number, aircraft_type,
                     total_seats, flight_status, base_price) VALUES
    (1, 'SU1234', 1, 'Москва', 'Сочи',
     DATE_ADD(CURDATE(), INTERVAL 2 DAY) + INTERVAL 9 HOUR + INTERVAL 30 MINUTE,
     DATE_ADD(CURDATE(), INTERVAL 2 DAY) + INTERVAL 12 HOUR + INTERVAL 10 MINUTE,
     'A12', 'Airbus A320', 180, 'SCHEDULED', 5400.00),
    (2, 'DP405', 2, 'Москва', 'Казань',
     DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 7 HOUR + INTERVAL 15 MINUTE,
     DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 8 HOUR + INTERVAL 50 MINUTE,
     'B3', 'Boeing 737-800', 189, 'SCHEDULED', 3900.00),
    (3, 'S72611', 3, 'Москва', 'Новосибирск',
     DATE_ADD(CURDATE(), INTERVAL 3 DAY) + INTERVAL 14 HOUR + INTERVAL 20 MINUTE,
     DATE_ADD(CURDATE(), INTERVAL 3 DAY) + INTERVAL 18 HOUR + INTERVAL 30 MINUTE,
     'C7', 'Airbus A320neo', 174, 'DELAYED', 7200.00),
    (4, 'U6123', 4, 'Екатеринбург', 'Москва',
     CURDATE() + INTERVAL 18 HOUR + INTERVAL 40 MINUTE,
     CURDATE() + INTERVAL 20 HOUR + INTERVAL 5 MINUTE,
     'D2', 'Airbus A321', 220, 'BOARDING', 6100.00),
    (5, 'N4301', 5, 'Москва', 'Калининград',
     DATE_ADD(CURDATE(), INTERVAL 5 DAY) + INTERVAL 11 HOUR,
     DATE_ADD(CURDATE(), INTERVAL 5 DAY) + INTERVAL 12 HOUR + INTERVAL 55 MINUTE,
     'A5', 'Boeing 737-800', 189, 'SCHEDULED', 4700.00),
    (6, 'SU2460', 1, 'Москва', 'Санкт-Петербург',
     CURDATE() + INTERVAL 20 HOUR + INTERVAL 15 MINUTE,
     CURDATE() + INTERVAL 21 HOUR + INTERVAL 40 MINUTE,
     'A8', 'Sukhoi Superjet 100', 98, 'SCHEDULED', 4300.00),
    (7, 'DP567', 2, 'Москва', 'Сочи',
     DATE_ADD(CURDATE(), INTERVAL 4 DAY) + INTERVAL 16 HOUR + INTERVAL 40 MINUTE,
     DATE_ADD(CURDATE(), INTERVAL 4 DAY) + INTERVAL 19 HOUR + INTERVAL 35 MINUTE,
     'B7', 'Boeing 737-800', 189, 'CANCELLED', 5100.00),
    (8, 'S7503', 3, 'Москва', 'Иркутск',
     DATE_ADD(CURDATE(), INTERVAL 6 DAY) + INTERVAL 22 HOUR + INTERVAL 10 MINUTE,
     DATE_ADD(CURDATE(), INTERVAL 7 DAY) + INTERVAL 6 HOUR + INTERVAL 50 MINUTE,
     'C2', 'Airbus A320', 174, 'SCHEDULED', 9400.00);

INSERT INTO bookings (id, flight_id, passenger_id, seat_number, ticket_class, price,
                      booking_time, booking_status, created_by) VALUES
    (1, 1, 1, '12A', 'ECONOMY', 5400.00, DATE_SUB(NOW(), INTERVAL 8 DAY), 'CONFIRMED', 2),
    (2, 1, 2, '12B', 'BUSINESS', 9720.00, DATE_SUB(NOW(), INTERVAL 7 DAY), 'CHECKED_IN', 2),
    (3, 2, 3, '5C', 'ECONOMY', 3900.00, DATE_SUB(NOW(), INTERVAL 6 DAY), 'CONFIRMED', 1),
    (4, 3, 4, '8A', 'ECONOMY', 7200.00, DATE_SUB(NOW(), INTERVAL 5 DAY), 'CONFIRMED', 2),
    (5, 4, 5, '15F', 'BUSINESS', 10980.00, DATE_SUB(NOW(), INTERVAL 4 DAY), 'CONFIRMED', 2),
    (6, 5, 1, '3A', 'ECONOMY', 4700.00, DATE_SUB(NOW(), INTERVAL 3 DAY), 'CANCELLED', 2),
    (7, 6, 2, '7C', 'ECONOMY', 4300.00, DATE_SUB(NOW(), INTERVAL 2 DAY), 'CHECKED_IN', 1),
    (8, 2, 4, '6D', 'FIRST', 10530.00, DATE_SUB(NOW(), INTERVAL 1 DAY), 'CONFIRMED', 2),
    (9, 3, 5, '9B', 'BUSINESS', 12960.00, DATE_SUB(NOW(), INTERVAL 1 DAY), 'CONFIRMED', 2);

ALTER TABLE users AUTO_INCREMENT = 3;
ALTER TABLE airlines AUTO_INCREMENT = 6;
ALTER TABLE passengers AUTO_INCREMENT = 6;
ALTER TABLE flights AUTO_INCREMENT = 9;
ALTER TABLE bookings AUTO_INCREMENT = 10;

-- Учётная запись для приложения (при необходимости раскомментируйте):
-- CREATE USER IF NOT EXISTS 'airport'@'localhost' IDENTIFIED BY 'airport';
-- GRANT ALL PRIVILEGES ON airport.* TO 'airport'@'localhost';
-- FLUSH PRIVILEGES;
