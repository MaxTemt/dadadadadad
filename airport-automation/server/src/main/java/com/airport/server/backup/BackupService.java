package com.airport.server.backup;

import com.airport.common.dto.BackupInfo;
import com.airport.common.exception.DataAccessException;
import com.airport.common.exception.NotFoundException;
import com.airport.common.exception.ValidationException;
import com.airport.common.model.Role;
import com.airport.common.util.Formats;
import com.airport.server.db.ConnectionPool;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Резервное копирование и восстановление данных в формате XML.
 * Копия создаётся на сервере в любой момент и может быть загружена обратно.
 */
public class BackupService {

    private static final String ROOT = "airport-backup";
    private static final String VERSION = "1";

    private final ConnectionPool pool;
    private final Path backupDir;

    public BackupService(ConnectionPool pool, Path backupDir) {
        this.pool = pool;
        this.backupDir = backupDir;
    }

    public BackupInfo createBackup(String actorName) throws DataAccessException, ValidationException {
        ensureDirectory();
        LocalDateTime now = LocalDateTime.now();
        String fileName = "backup-" + Formats.FILE_STAMP.format(now) + ".xml";
        Path target = backupDir.resolve(fileName);
        int counter = 1;
        while (Files.exists(target)) {
            target = backupDir.resolve("backup-" + Formats.FILE_STAMP.format(now) + "-" + counter + ".xml");
            counter++;
        }

        try {
            Document document = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().newDocument();
            Element root = document.createElement(ROOT);
            root.setAttribute("version", VERSION);
            root.setAttribute("createdAt", now.toString());
            root.setAttribute("createdBy", actorName == null ? "" : actorName);
            document.appendChild(root);

            appendUsers(document, root);
            appendAirlines(document, root);
            appendPassengers(document, root);
            appendFlights(document, root);
            appendBookings(document, root);

            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            try (OutputStream output = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE)) {
                transformer.transform(new DOMSource(document), new StreamResult(output));
            }
            return new BackupInfo(fileName, Files.size(target), now);
        } catch (ValidationException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new DataAccessException("Не удалось создать резервную копию: " + exception.getMessage(),
                    exception);
        }
    }

    public List<BackupInfo> listBackups() throws DataAccessException {
        ensureDirectory();
        List<BackupInfo> result = new ArrayList<>();
        try (Stream<Path> files = Files.list(backupDir)) {
            files.filter(path -> Files.isRegularFile(path)
                            && path.getFileName().toString().endsWith(".xml"))
                    .forEach(path -> {
                        try {
                            result.add(new BackupInfo(path.getFileName().toString(), Files.size(path),
                                    LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(),
                                            ZoneId.systemDefault())));
                        } catch (IOException ignored) {
                            // файл мог быть удалён параллельно
                        }
                    });
        } catch (IOException exception) {
            throw new DataAccessException("Не удалось прочитать каталог резервных копий", exception);
        }
        result.sort(Comparator.reverseOrder());
        return result;
    }

    public String restoreBackup(String fileName) throws DataAccessException, ValidationException,
            NotFoundException {
        Path path = resolveSafe(fileName);
        if (!Files.isRegularFile(path)) {
            throw new NotFoundException("Резервная копия не найдена: " + fileName);
        }
        try {
            if (Files.size(path) > 50L * 1024 * 1024) {
                throw new ValidationException("Файл резервной копии слишком большой");
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            Document document;
            try (InputStream input = Files.newInputStream(path)) {
                document = factory.newDocumentBuilder().parse(input);
            }
            Element root = document.getDocumentElement();
            if (root == null || !ROOT.equals(root.getNodeName())) {
                throw new ValidationException("Файл не является резервной копией системы");
            }
            if (!VERSION.equals(root.getAttribute("version"))) {
                throw new ValidationException("Неподдерживаемая версия резервной копии");
            }

            List<Element> users = children(root, "users", "user");
            List<Element> airlines = children(root, "airlines", "airline");
            List<Element> passengers = children(root, "passengers", "passenger");
            List<Element> flights = children(root, "flights", "flight");
            List<Element> bookings = children(root, "bookings", "booking");
            validateBackup(users, airlines, passengers, flights, bookings);

            pool.withTransaction(connection -> {
                execute(connection, "DELETE FROM bookings");
                execute(connection, "DELETE FROM flights");
                execute(connection, "DELETE FROM passengers");
                execute(connection, "DELETE FROM airlines");
                execute(connection, "DELETE FROM users");

                insertUsers(connection, users);
                insertAirlines(connection, airlines);
                insertPassengers(connection, passengers);
                insertFlights(connection, flights);
                insertBookings(connection, bookings);
                return null;
            });
            resetSequences();
            return "Восстановлено: пользователей " + users.size() + ", авиакомпаний " + airlines.size()
                    + ", пассажиров " + passengers.size() + ", рейсов " + flights.size()
                    + ", бронирований " + bookings.size();
        } catch (ValidationException | DataAccessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new DataAccessException("Не удалось восстановить резервную копию: "
                    + exception.getMessage(), exception);
        }
    }

    private void appendUsers(Document document, Element root) throws Exception {
        Element container = document.createElement("users");
        root.appendChild(container);
        Connection connection = pool.borrow();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT id, username, password_hash, user_role,"
                     + " first_name, last_name, email, phone, active, created_at"
                     + " FROM users ORDER BY id")) {
            while (rs.next()) {
                Element element = document.createElement("user");
                attr(element, "id", rs.getLong("id"));
                attr(element, "username", rs.getString("username"));
                attr(element, "passwordHash", rs.getString("password_hash"));
                attr(element, "role", rs.getString("user_role"));
                attr(element, "firstName", rs.getString("first_name"));
                attr(element, "lastName", rs.getString("last_name"));
                attr(element, "email", rs.getString("email"));
                attr(element, "phone", rs.getString("phone"));
                attr(element, "active", rs.getBoolean("active"));
                attr(element, "createdAt", dateTime(rs, "created_at"));
                container.appendChild(element);
            }
        } finally {
            pool.release(connection);
        }
    }

    private void appendAirlines(Document document, Element root) throws Exception {
        Element container = document.createElement("airlines");
        root.appendChild(container);
        Connection connection = pool.borrow();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT id, name, iata_code, country, contact_phone"
                     + " FROM airlines ORDER BY id")) {
            while (rs.next()) {
                Element element = document.createElement("airline");
                attr(element, "id", rs.getLong("id"));
                attr(element, "name", rs.getString("name"));
                attr(element, "iataCode", rs.getString("iata_code"));
                attr(element, "country", rs.getString("country"));
                attr(element, "contactPhone", rs.getString("contact_phone"));
                container.appendChild(element);
            }
        } finally {
            pool.release(connection);
        }
    }

    private void appendPassengers(Document document, Element root) throws Exception {
        Element container = document.createElement("passengers");
        root.appendChild(container);
        Connection connection = pool.borrow();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT id, first_name, last_name, passport_number,"
                     + " email, phone, date_of_birth, frequent_flyer_level, created_at"
                     + " FROM passengers ORDER BY id")) {
            while (rs.next()) {
                Element element = document.createElement("passenger");
                attr(element, "id", rs.getLong("id"));
                attr(element, "firstName", rs.getString("first_name"));
                attr(element, "lastName", rs.getString("last_name"));
                attr(element, "passportNumber", rs.getString("passport_number"));
                attr(element, "email", rs.getString("email"));
                attr(element, "phone", rs.getString("phone"));
                attr(element, "dateOfBirth", date(rs, "date_of_birth"));
                attr(element, "frequentFlyerLevel", rs.getString("frequent_flyer_level"));
                attr(element, "createdAt", dateTime(rs, "created_at"));
                container.appendChild(element);
            }
        } finally {
            pool.release(connection);
        }
    }

    private void appendFlights(Document document, Element root) throws Exception {
        Element container = document.createElement("flights");
        root.appendChild(container);
        Connection connection = pool.borrow();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT id, flight_number, airline_id, origin_city,"
                     + " destination_city, departure_time, arrival_time, gate_number, aircraft_type,"
                     + " total_seats, flight_status, base_price FROM flights ORDER BY id")) {
            while (rs.next()) {
                Element element = document.createElement("flight");
                attr(element, "id", rs.getLong("id"));
                attr(element, "flightNumber", rs.getString("flight_number"));
                attr(element, "airlineId", rs.getLong("airline_id"));
                attr(element, "originCity", rs.getString("origin_city"));
                attr(element, "destinationCity", rs.getString("destination_city"));
                attr(element, "departureTime", dateTime(rs, "departure_time"));
                attr(element, "arrivalTime", dateTime(rs, "arrival_time"));
                attr(element, "gate", rs.getString("gate_number"));
                attr(element, "aircraftType", rs.getString("aircraft_type"));
                attr(element, "totalSeats", rs.getInt("total_seats"));
                attr(element, "status", rs.getString("flight_status"));
                attr(element, "basePrice", rs.getBigDecimal("base_price"));
                container.appendChild(element);
            }
        } finally {
            pool.release(connection);
        }
    }

    private void appendBookings(Document document, Element root) throws Exception {
        Element container = document.createElement("bookings");
        root.appendChild(container);
        Connection connection = pool.borrow();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT id, flight_id, passenger_id, seat_number,"
                     + " ticket_class, price, booking_time, booking_status, created_by"
                     + " FROM bookings ORDER BY id")) {
            while (rs.next()) {
                Element element = document.createElement("booking");
                attr(element, "id", rs.getLong("id"));
                attr(element, "flightId", rs.getLong("flight_id"));
                attr(element, "passengerId", rs.getLong("passenger_id"));
                attr(element, "seatNumber", rs.getString("seat_number"));
                attr(element, "ticketClass", rs.getString("ticket_class"));
                attr(element, "price", rs.getBigDecimal("price"));
                attr(element, "bookingTime", dateTime(rs, "booking_time"));
                attr(element, "status", rs.getString("booking_status"));
                attr(element, "createdBy", rs.getLong("created_by"));
                container.appendChild(element);
            }
        } finally {
            pool.release(connection);
        }
    }

    private void validateBackup(List<Element> users, List<Element> airlines, List<Element> passengers,
                                List<Element> flights, List<Element> bookings) throws ValidationException {
        if (users.isEmpty()) {
            throw new ValidationException("Резервная копия не содержит пользователей");
        }
        boolean admin = users.stream()
                .anyMatch(user -> Role.ADMIN.name().equals(user.getAttribute("role"))
                        && Boolean.parseBoolean(user.getAttribute("active")));
        if (!admin) {
            throw new ValidationException("В резервной копии нет активного администратора");
        }
        Set<Long> userIds = ids(users);
        Set<Long> airlineIds = ids(airlines);
        Set<Long> passengerIds = ids(passengers);
        Set<Long> flightIds = ids(flights);
        for (Element flight : flights) {
            if (!airlineIds.contains(longAttr(flight, "airlineId"))) {
                throw new ValidationException("Рейс " + flight.getAttribute("flightNumber")
                        + " ссылается на отсутствующую авиакомпанию");
            }
        }
        for (Element booking : bookings) {
            if (!flightIds.contains(longAttr(booking, "flightId"))
                    || !passengerIds.contains(longAttr(booking, "passengerId"))
                    || !userIds.contains(longAttr(booking, "createdBy"))) {
                throw new ValidationException("Бронирование " + booking.getAttribute("id")
                        + " содержит некорректные ссылки");
            }
        }
    }

    private Set<Long> ids(List<Element> elements) {
        Set<Long> result = new HashSet<>();
        for (Element element : elements) {
            result.add(longAttr(element, "id"));
        }
        return result;
    }

    private void insertUsers(Connection connection, List<Element> users) throws SQLException {
        String sql = "INSERT INTO users (id, username, password_hash, user_role, first_name, last_name,"
                + " email, phone, active, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Element user : users) {
                bind(statement, longAttr(user, "id"), user.getAttribute("username"),
                        user.getAttribute("passwordHash"), user.getAttribute("role"),
                        user.getAttribute("firstName"), user.getAttribute("lastName"),
                        nullable(user, "email"), nullable(user, "phone"),
                        Boolean.parseBoolean(user.getAttribute("active")),
                        dateTimeAttr(user, "createdAt", true));
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void insertAirlines(Connection connection, List<Element> airlines) throws SQLException {
        String sql = "INSERT INTO airlines (id, name, iata_code, country, contact_phone)"
                + " VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Element airline : airlines) {
                bind(statement, longAttr(airline, "id"), airline.getAttribute("name"),
                        airline.getAttribute("iataCode"), airline.getAttribute("country"),
                        nullable(airline, "contactPhone"));
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void insertPassengers(Connection connection, List<Element> passengers) throws SQLException {
        String sql = "INSERT INTO passengers (id, first_name, last_name, passport_number, email, phone,"
                + " date_of_birth, frequent_flyer_level, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Element passenger : passengers) {
                bind(statement, longAttr(passenger, "id"), passenger.getAttribute("firstName"),
                        passenger.getAttribute("lastName"), passenger.getAttribute("passportNumber"),
                        nullable(passenger, "email"), nullable(passenger, "phone"),
                        dateAttr(passenger, "dateOfBirth"),
                        passenger.getAttribute("frequentFlyerLevel"),
                        dateTimeAttr(passenger, "createdAt", true));
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void insertFlights(Connection connection, List<Element> flights) throws SQLException {
        String sql = "INSERT INTO flights (id, flight_number, airline_id, origin_city, destination_city,"
                + " departure_time, arrival_time, gate_number, aircraft_type, total_seats, flight_status,"
                + " base_price) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Element flight : flights) {
                bind(statement, longAttr(flight, "id"), flight.getAttribute("flightNumber"),
                        longAttr(flight, "airlineId"), flight.getAttribute("originCity"),
                        flight.getAttribute("destinationCity"),
                        dateTimeAttr(flight, "departureTime", false),
                        dateTimeAttr(flight, "arrivalTime", false),
                        nullable(flight, "gate"), nullable(flight, "aircraftType"),
                        Integer.parseInt(flight.getAttribute("totalSeats")),
                        flight.getAttribute("status"),
                        new BigDecimal(flight.getAttribute("basePrice")));
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void insertBookings(Connection connection, List<Element> bookings) throws SQLException {
        String sql = "INSERT INTO bookings (id, flight_id, passenger_id, seat_number, ticket_class,"
                + " price, booking_time, booking_status, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Element booking : bookings) {
                bind(statement, longAttr(booking, "id"), longAttr(booking, "flightId"),
                        longAttr(booking, "passengerId"), booking.getAttribute("seatNumber"),
                        booking.getAttribute("ticketClass"),
                        new BigDecimal(booking.getAttribute("price")),
                        dateTimeAttr(booking, "bookingTime", true),
                        booking.getAttribute("status"), longAttr(booking, "createdBy"));
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void resetSequences() {
        String[] tables = {"users", "airlines", "passengers", "flights", "bookings"};
        for (String table : tables) {
            try {
                long max = pool.withConnection(connection -> {
                    try (Statement statement = connection.createStatement();
                         ResultSet rs = statement.executeQuery("SELECT COALESCE(MAX(id), 0) FROM " + table)) {
                        return rs.next() ? rs.getLong(1) : 0L;
                    }
                });
                long next = max + 1;
                Connection connection = pool.borrow();
                try (Statement statement = connection.createStatement()) {
                    try {
                        statement.execute("ALTER TABLE " + table + " ALTER COLUMN id RESTART WITH " + next);
                    } catch (SQLException h2Exception) {
                        statement.execute("ALTER TABLE " + table + " AUTO_INCREMENT = " + next);
                    }
                } catch (SQLException ignored) {
                    // для MySQL счётчик обновляется автоматически при вставке явных id
                } finally {
                    pool.release(connection);
                }
            } catch (DataAccessException ignored) {
                // не критично для восстановления данных
            }
        }
    }

    private static void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private static void attr(Element element, String name, Object value) {
        if (value == null) {
            return;
        }
        element.setAttribute(name, value.toString());
    }

    private static String nullable(Element element, String name) {
        String value = element.getAttribute(name);
        return value == null || value.isBlank() ? null : value;
    }

    private static long longAttr(Element element, String name) {
        return Long.parseLong(element.getAttribute(name));
    }

    private static LocalDateTime dateTimeAttr(Element element, String name, boolean nullable)
            throws SQLException {
        String value = element.getAttribute(name);
        if (value == null || value.isBlank()) {
            if (nullable) {
                return null;
            }
            throw new SQLException("Отсутствует обязательное поле " + name);
        }
        try {
            return LocalDateTime.parse(value);
        } catch (RuntimeException exception) {
            throw new SQLException("Некорректное значение даты " + name + ": " + value, exception);
        }
    }

    private static LocalDate dateAttr(Element element, String name) throws SQLException {
        String value = element.getAttribute(name);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (RuntimeException exception) {
            throw new SQLException("Некорректное значение даты " + name + ": " + value, exception);
        }
    }

    private static LocalDateTime dateTime(ResultSet resultSet, String column) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp(column);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private static LocalDate date(ResultSet resultSet, String column) throws SQLException {
        Date date = resultSet.getDate(column);
        return date == null ? null : date.toLocalDate();
    }

    private static void bind(PreparedStatement statement, Object... parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            Object value = parameters[i];
            int index = i + 1;
            if (value == null) {
                statement.setNull(index, Types.NULL);
            } else if (value instanceof LocalDateTime dateTime) {
                statement.setTimestamp(index, Timestamp.valueOf(dateTime));
            } else if (value instanceof LocalDate date) {
                statement.setDate(index, Date.valueOf(date));
            } else if (value instanceof BigDecimal decimal) {
                statement.setBigDecimal(index, decimal);
            } else {
                statement.setObject(index, value);
            }
        }
    }

    private static List<Element> children(Element root, String containerName, String itemName) {
        List<Element> result = new ArrayList<>();
        NodeList containers = root.getElementsByTagName(containerName);
        if (containers.getLength() == 0) {
            return result;
        }
        NodeList items = ((Element) containers.item(0)).getElementsByTagName(itemName);
        for (int i = 0; i < items.getLength(); i++) {
            Node node = items.item(i);
            if (node instanceof Element element) {
                result.add(element);
            }
        }
        return result;
    }

    private void ensureDirectory() throws DataAccessException {
        try {
            Files.createDirectories(backupDir);
        } catch (IOException exception) {
            throw new DataAccessException("Не удалось создать каталог резервных копий: " + backupDir,
                    exception);
        }
    }

    private Path resolveSafe(String fileName) throws ValidationException {
        if (fileName == null || fileName.isBlank()) {
            throw new ValidationException("Не указано имя файла резервной копии");
        }
        Path path = backupDir.resolve(Path.of(fileName).getFileName()).normalize();
        if (!path.startsWith(backupDir.normalize())) {
            throw new ValidationException("Некорректное имя файла резервной копии");
        }
        return path;
    }
}
