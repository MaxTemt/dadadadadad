-- Схема для интеграционных тестов (H2 в режиме совместимости с MySQL).
-- Имена таблиц и столбцов совпадают с db/schema.sql.

DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS flights;
DROP TABLE IF EXISTS passengers;
DROP TABLE IF EXISTS airlines;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(30)  NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    user_role     VARCHAR(10)  NOT NULL DEFAULT 'USER',
    first_name    VARCHAR(50)  NOT NULL,
    last_name     VARCHAR(50)  NOT NULL,
    email         VARCHAR(100),
    phone         VARCHAR(30),
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT chk_users_role CHECK (user_role IN ('ADMIN', 'USER'))
);

CREATE TABLE airlines (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    iata_code     VARCHAR(3)   NOT NULL,
    country       VARCHAR(80)  NOT NULL,
    contact_phone VARCHAR(30),
    CONSTRAINT uk_airlines_name UNIQUE (name),
    CONSTRAINT uk_airlines_iata UNIQUE (iata_code)
);

CREATE TABLE passengers (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name           VARCHAR(50) NOT NULL,
    last_name            VARCHAR(50) NOT NULL,
    passport_number      VARCHAR(20) NOT NULL,
    email                VARCHAR(100),
    phone                VARCHAR(30),
    date_of_birth        DATE        NOT NULL,
    frequent_flyer_level VARCHAR(10) NOT NULL DEFAULT 'NONE',
    created_at           TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_passengers_passport UNIQUE (passport_number),
    CONSTRAINT chk_passengers_level
        CHECK (frequent_flyer_level IN ('NONE', 'SILVER', 'GOLD', 'PLATINUM'))
);

CREATE TABLE flights (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    flight_number    VARCHAR(10)   NOT NULL,
    airline_id       BIGINT        NOT NULL,
    origin_city      VARCHAR(80)   NOT NULL,
    destination_city VARCHAR(80)   NOT NULL,
    departure_time   TIMESTAMP     NOT NULL,
    arrival_time     TIMESTAMP     NOT NULL,
    gate_number      VARCHAR(10),
    aircraft_type    VARCHAR(50),
    total_seats      INT           NOT NULL,
    flight_status    VARCHAR(12)   NOT NULL DEFAULT 'SCHEDULED',
    base_price       DECIMAL(12, 2) NOT NULL,
    CONSTRAINT uk_flights_number UNIQUE (flight_number),
    CONSTRAINT fk_flights_airline FOREIGN KEY (airline_id) REFERENCES airlines (id),
    CONSTRAINT chk_flights_status CHECK (flight_status IN
        ('SCHEDULED', 'BOARDING', 'DEPARTED', 'DELAYED', 'CANCELLED')),
    CONSTRAINT chk_flights_seats CHECK (total_seats BETWEEN 1 AND 1000),
    CONSTRAINT chk_flights_price CHECK (base_price > 0),
    CONSTRAINT chk_flights_time CHECK (arrival_time > departure_time)
);

CREATE TABLE bookings (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    flight_id      BIGINT        NOT NULL,
    passenger_id   BIGINT        NOT NULL,
    seat_number    VARCHAR(5)    NOT NULL,
    ticket_class   VARCHAR(10)   NOT NULL DEFAULT 'ECONOMY',
    price          DECIMAL(12, 2) NOT NULL,
    booking_time   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    booking_status VARCHAR(12)   NOT NULL DEFAULT 'CONFIRMED',
    created_by     BIGINT        NOT NULL,
    CONSTRAINT fk_bookings_flight FOREIGN KEY (flight_id) REFERENCES flights (id),
    CONSTRAINT fk_bookings_passenger FOREIGN KEY (passenger_id) REFERENCES passengers (id),
    CONSTRAINT fk_bookings_user FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT chk_bookings_class CHECK (ticket_class IN ('ECONOMY', 'BUSINESS', 'FIRST')),
    CONSTRAINT chk_bookings_status CHECK (booking_status IN
        ('CONFIRMED', 'CHECKED_IN', 'CANCELLED')),
    CONSTRAINT chk_bookings_price CHECK (price > 0)
);

CREATE INDEX idx_flights_airline ON flights (airline_id);
CREATE INDEX idx_flights_departure ON flights (departure_time);
CREATE INDEX idx_bookings_flight ON bookings (flight_id);
CREATE INDEX idx_bookings_passenger ON bookings (passenger_id);
CREATE INDEX idx_bookings_user ON bookings (created_by);

-- Демонстрационные учётные записи (admin123 / agent123)
INSERT INTO users (id, username, password_hash, user_role, first_name, last_name,
                   email, phone, active, created_at) VALUES
    (1, 'admin',
     'pbkdf2$120000$D5Vmv3yTwDnv7xZLD7k/lA==$Gyje29TupWWmnGEntVh2WCHIYHmoFBCyQEIeajqfjCo=',
     'ADMIN', 'Игорь', 'Смирнов', 'admin@airport.local', '+7 (495) 100-10-10', TRUE,
     DATEADD('DAY', -90, CURRENT_TIMESTAMP)),
    (2, 'agent',
     'pbkdf2$120000$x/gKwGIECehIiZQ0k2jgsQ==$gzmGaouz33a/srpqM4eMbo68VwWUxgnlkS99bAdmAms=',
     'USER', 'Анна', 'Кузнецова', 'agent@airport.local', '+7 (495) 100-20-20', TRUE,
     DATEADD('DAY', -60, CURRENT_TIMESTAMP));

INSERT INTO airlines (id, name, iata_code, country, contact_phone) VALUES
    (1, 'Аэрофлот', 'SU', 'Россия', '+7 (495) 223-55-55'),
    (2, 'Победа', 'DP', 'Россия', '+7 (809) 505-40-70'),
    (3, 'S7 Airlines', 'S7', 'Россия', '+7 (800) 700-07-07'),
    (4, 'Уральские авиалинии', 'U6', 'Россия', '+7 (800) 770-00-10'),
    (5, 'Nordwind Airlines', 'N4', 'Россия', '+7 (495) 730-50-50');

INSERT INTO passengers (id, first_name, last_name, passport_number, email, phone,
                        date_of_birth, frequent_flyer_level, created_at) VALUES
    (1, 'Пётр', 'Иванов', '4510 123456', 'petr.ivanov@example.com', '+7 (916) 111-22-33',
     DATE '1985-04-12', 'GOLD', DATEADD('DAY', -40, CURRENT_TIMESTAMP)),
    (2, 'Мария', 'Петрова', '4511 654321', 'maria.petrova@example.com', '+7 (916) 222-33-44',
     DATE '1990-09-23', 'PLATINUM', DATEADD('DAY', -35, CURRENT_TIMESTAMP)),
    (3, 'Алексей', 'Соколов', '4512 777888', 'alexey.sokolov@example.com', '+7 (916) 333-44-55',
     DATE '1978-01-30', 'SILVER', DATEADD('DAY', -20, CURRENT_TIMESTAMP)),
    (4, 'Елена', 'Морозова', '4513 999000', 'elena.morozova@example.com', '+7 (916) 444-55-66',
     DATE '1995-12-05', 'NONE', DATEADD('DAY', -10, CURRENT_TIMESTAMP)),
    (5, 'Дмитрий', 'Волков', '4514 121212', 'dmitry.volkov@example.com', '+7 (916) 555-66-77',
     DATE '1982-06-18', 'GOLD', DATEADD('DAY', -5, CURRENT_TIMESTAMP));

INSERT INTO flights (id, flight_number, airline_id, origin_city, destination_city,
                     departure_time, arrival_time, gate_number, aircraft_type,
                     total_seats, flight_status, base_price) VALUES
    (1, 'SU1234', 1, 'Москва', 'Сочи',
     DATEADD('DAY', 2, CURRENT_TIMESTAMP), DATEADD('DAY', 2, DATEADD('HOUR', 3, CURRENT_TIMESTAMP)),
     'A12', 'Airbus A320', 180, 'SCHEDULED', 5400.00),
    (2, 'DP405', 2, 'Москва', 'Казань',
     DATEADD('DAY', 1, CURRENT_TIMESTAMP), DATEADD('DAY', 1, DATEADD('HOUR', 2, CURRENT_TIMESTAMP)),
     'B3', 'Boeing 737-800', 189, 'SCHEDULED', 3900.00),
    (3, 'S72611', 3, 'Москва', 'Новосибирск',
     DATEADD('DAY', 3, CURRENT_TIMESTAMP), DATEADD('DAY', 3, DATEADD('HOUR', 4, CURRENT_TIMESTAMP)),
     'C7', 'Airbus A320neo', 174, 'DELAYED', 7200.00),
    (4, 'U6123', 4, 'Екатеринбург', 'Москва',
     DATEADD('DAY', 4, CURRENT_TIMESTAMP), DATEADD('DAY', 4, DATEADD('HOUR', 2, CURRENT_TIMESTAMP)),
     'D2', 'Airbus A321', 220, 'BOARDING', 6100.00),
    (5, 'N4301', 5, 'Москва', 'Калининград',
     DATEADD('DAY', 5, CURRENT_TIMESTAMP), DATEADD('DAY', 5, DATEADD('HOUR', 2, CURRENT_TIMESTAMP)),
     'A5', 'Boeing 737-800', 189, 'SCHEDULED', 4700.00),
    (6, 'SU2460', 1, 'Москва', 'Санкт-Петербург',
     DATEADD('DAY', 6, CURRENT_TIMESTAMP), DATEADD('DAY', 6, DATEADD('HOUR', 2, CURRENT_TIMESTAMP)),
     'A8', 'Sukhoi Superjet 100', 98, 'SCHEDULED', 4300.00),
    (7, 'DP567', 2, 'Москва', 'Сочи',
     DATEADD('DAY', 7, CURRENT_TIMESTAMP), DATEADD('DAY', 7, DATEADD('HOUR', 3, CURRENT_TIMESTAMP)),
     'B7', 'Boeing 737-800', 189, 'CANCELLED', 5100.00),
    (8, 'S7503', 3, 'Москва', 'Иркутск',
     DATEADD('DAY', 8, CURRENT_TIMESTAMP), DATEADD('DAY', 8, DATEADD('HOUR', 6, CURRENT_TIMESTAMP)),
     'C2', 'Airbus A320', 174, 'SCHEDULED', 9400.00);

INSERT INTO bookings (id, flight_id, passenger_id, seat_number, ticket_class, price,
                      booking_time, booking_status, created_by) VALUES
    (1, 1, 1, '12A', 'ECONOMY', 5400.00, DATEADD('DAY', -8, CURRENT_TIMESTAMP), 'CONFIRMED', 2),
    (2, 1, 2, '12B', 'BUSINESS', 9720.00, DATEADD('DAY', -7, CURRENT_TIMESTAMP), 'CHECKED_IN', 2),
    (3, 2, 3, '5C', 'ECONOMY', 3900.00, DATEADD('DAY', -6, CURRENT_TIMESTAMP), 'CONFIRMED', 1),
    (4, 3, 4, '8A', 'ECONOMY', 7200.00, DATEADD('DAY', -5, CURRENT_TIMESTAMP), 'CONFIRMED', 2),
    (5, 4, 5, '15F', 'BUSINESS', 10980.00, DATEADD('DAY', -4, CURRENT_TIMESTAMP), 'CONFIRMED', 2),
    (6, 5, 1, '3A', 'ECONOMY', 4700.00, DATEADD('DAY', -3, CURRENT_TIMESTAMP), 'CANCELLED', 2),
    (7, 6, 2, '7C', 'ECONOMY', 4300.00, DATEADD('DAY', -2, CURRENT_TIMESTAMP), 'CHECKED_IN', 1),
    (8, 2, 4, '6D', 'FIRST', 10530.00, DATEADD('DAY', -1, CURRENT_TIMESTAMP), 'CONFIRMED', 2),
    (9, 3, 5, '9B', 'BUSINESS', 12960.00, DATEADD('DAY', -1, CURRENT_TIMESTAMP), 'CONFIRMED', 2);

ALTER TABLE users ALTER COLUMN id RESTART WITH 3;
ALTER TABLE airlines ALTER COLUMN id RESTART WITH 6;
ALTER TABLE passengers ALTER COLUMN id RESTART WITH 6;
ALTER TABLE flights ALTER COLUMN id RESTART WITH 9;
ALTER TABLE bookings ALTER COLUMN id RESTART WITH 10;
