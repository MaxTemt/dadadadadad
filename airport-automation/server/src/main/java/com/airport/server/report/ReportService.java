package com.airport.server.report;

import com.airport.common.dto.ReportInfo;
import com.airport.common.dto.ReportResult;
import com.airport.common.exception.DataAccessException;
import com.airport.common.exception.NotFoundException;
import com.airport.common.exception.ValidationException;
import com.airport.common.model.Booking;
import com.airport.common.model.Flight;
import com.airport.common.util.Formats;
import com.airport.server.dao.BookingDao;
import com.airport.server.dao.BookingFilter;
import com.airport.server.dao.FlightDao;
import com.airport.server.dao.FlightFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Формирование текстовых отчётов. Отчёт сохраняется на сервере и передаётся клиенту.
 */
public class ReportService {

    private static final String SEPARATOR = "=".repeat(132);
    private static final String THIN_SEPARATOR = "-".repeat(132);

    private final FlightDao flightDao;
    private final BookingDao bookingDao;
    private final Path reportsDir;

    public ReportService(FlightDao flightDao, BookingDao bookingDao, Path reportsDir) {
        this.flightDao = flightDao;
        this.bookingDao = bookingDao;
        this.reportsDir = reportsDir;
    }

    public ReportResult generateFlightsReport(String actorName) throws DataAccessException, ValidationException {
        List<Flight> flights = flightDao.search(new FlightFilter());
        if (flights.isEmpty()) {
            throw new ValidationException("Нет данных для формирования отчёта");
        }
        StringBuilder text = new StringBuilder();
        text.append(SEPARATOR).append(System.lineSeparator());
        text.append("ОТЧЁТ ПО РЕЙСАМ АЭРОПОРТА").append(System.lineSeparator());
        text.append("Сформирован: ").append(Formats.dateTime(LocalDateTime.now()))
                .append("   Пользователь: ").append(actorName).append(System.lineSeparator());
        text.append(SEPARATOR).append(System.lineSeparator());
        text.append(String.format("%-4s %-8s %-20s %-30s %-16s %-16s %-6s %-9s %-18s %10s%n",
                "№", "Рейс", "Авиакомпания", "Маршрут", "Вылет", "Прилёт", "Выход",
                "Места", "Статус", "Цена"));
        text.append(THIN_SEPARATOR).append(System.lineSeparator());

        int index = 1;
        int totalSeats = 0;
        int bookedSeats = 0;
        for (Flight flight : flights) {
            totalSeats += flight.getTotalSeats();
            bookedSeats += flight.getBookedSeats();
            text.append(String.format("%-4d %-8s %-20s %-30s %-16s %-16s %-6s %-9s %-18s %10s%n",
                    index++,
                    fit(flight.getFlightNumber(), 8),
                    fit(flight.getAirlineName(), 20),
                    fit(flight.getOriginCity() + " - " + flight.getDestinationCity(), 30),
                    fit(Formats.dateTime(flight.getDepartureTime()), 16),
                    fit(Formats.dateTime(flight.getArrivalTime()), 16),
                    fit(flight.getGate(), 6),
                    flight.getBookedSeats() + "/" + flight.getTotalSeats(),
                    fit(flight.getStatus().getTitle(), 18),
                    Formats.money(flight.getBasePrice())));
        }
        text.append(THIN_SEPARATOR).append(System.lineSeparator());
        text.append("Всего рейсов: ").append(flights.size())
                .append("; мест: ").append(totalSeats)
                .append("; занято: ").append(bookedSeats)
                .append("; свободно: ").append(totalSeats - bookedSeats).append(System.lineSeparator());
        text.append(SEPARATOR).append(System.lineSeparator());
        return write("flights", text.toString());
    }

    public ReportResult generateBookingsReport(String actorName) throws DataAccessException, ValidationException {
        List<Booking> bookings = bookingDao.search(new BookingFilter());
        if (bookings.isEmpty()) {
            throw new ValidationException("Нет данных для формирования отчёта");
        }
        StringBuilder text = new StringBuilder();
        text.append(SEPARATOR).append(System.lineSeparator());
        text.append("ОТЧЁТ ПО БРОНИРОВАНИЯМ").append(System.lineSeparator());
        text.append("Сформирован: ").append(Formats.dateTime(LocalDateTime.now()))
                .append("   Пользователь: ").append(actorName).append(System.lineSeparator());
        text.append(SEPARATOR).append(System.lineSeparator());
        text.append(String.format("%-4s %-8s %-28s %-7s %-12s %10s %-20s %-16s%n",
                "№", "Рейс", "Пассажир", "Место", "Класс", "Стоимость", "Статус", "Оформлено"));
        text.append(THIN_SEPARATOR).append(System.lineSeparator());

        int index = 1;
        double revenue = 0;
        for (Booking booking : bookings) {
            if (booking.getStatus() != com.airport.common.model.BookingStatus.CANCELLED) {
                revenue += booking.getPrice().doubleValue();
            }
            text.append(String.format("%-4d %-8s %-28s %-7s %-12s %10s %-20s %-16s%n",
                    index++,
                    fit(booking.getFlightNumber(), 8),
                    fit(booking.getPassengerName(), 28),
                    fit(booking.getSeatNumber(), 7),
                    fit(booking.getTicketClass().getTitle(), 12),
                    Formats.money(booking.getPrice()),
                    fit(booking.getStatus().getTitle(), 20),
                    fit(Formats.dateTime(booking.getBookingTime()), 16)));
        }
        text.append(THIN_SEPARATOR).append(System.lineSeparator());
        text.append("Всего бронирований: ").append(bookings.size())
                .append("; выручка по действующим броням: ")
                .append(Formats.money(revenue)).append(" руб.").append(System.lineSeparator());
        text.append(SEPARATOR).append(System.lineSeparator());
        return write("bookings", text.toString());
    }

    public List<ReportInfo> listReports() throws DataAccessException {
        ensureDirectory();
        List<ReportInfo> result = new ArrayList<>();
        try (Stream<Path> files = Files.list(reportsDir)) {
            files.filter(path -> Files.isRegularFile(path)
                            && path.getFileName().toString().endsWith(".txt"))
                    .forEach(path -> {
                        try {
                            result.add(new ReportInfo(path.getFileName().toString(),
                                    Files.size(path),
                                    LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(),
                                            ZoneId.systemDefault())));
                        } catch (IOException ignored) {
                            // пропускаем файл, который не удалось прочитать
                        }
                    });
        } catch (IOException exception) {
            throw new DataAccessException("Не удалось прочитать каталог отчётов", exception);
        }
        result.sort(Comparator.reverseOrder());
        return result;
    }

    public ReportResult readReport(String fileName) throws DataAccessException, NotFoundException,
            ValidationException {
        Path path = resolveSafe(fileName);
        if (!Files.isRegularFile(path)) {
            throw new NotFoundException("Отчёт не найден: " + fileName);
        }
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            return new ReportResult(path.getFileName().toString(), content,
                    LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(),
                            ZoneId.systemDefault()));
        } catch (IOException exception) {
            throw new DataAccessException("Не удалось прочитать отчёт", exception);
        }
    }

    private ReportResult write(String prefix, String content) throws DataAccessException {
        ensureDirectory();
        LocalDateTime now = LocalDateTime.now();
        String baseName = prefix + "-report-" + Formats.FILE_STAMP.format(now);
        Path target = reportsDir.resolve(baseName + ".txt");
        int counter = 1;
        while (Files.exists(target)) {
            target = reportsDir.resolve(baseName + "-" + counter + ".txt");
            counter++;
        }
        try {
            Files.writeString(target, content, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        } catch (IOException exception) {
            throw new DataAccessException("Не удалось сохранить отчёт на сервере", exception);
        }
        return new ReportResult(target.getFileName().toString(), content, now);
    }

    private void ensureDirectory() throws DataAccessException {
        try {
            Files.createDirectories(reportsDir);
        } catch (IOException exception) {
            throw new DataAccessException("Не удалось создать каталог отчётов: " + reportsDir, exception);
        }
    }

    private Path resolveSafe(String fileName) throws ValidationException {
        if (fileName == null || fileName.isBlank()) {
            throw new ValidationException("Не указано имя отчёта");
        }
        Path path = reportsDir.resolve(Path.of(fileName).getFileName()).normalize();
        if (!path.startsWith(reportsDir.normalize())) {
            throw new ValidationException("Некорректное имя файла отчёта");
        }
        return path;
    }

    private String fit(String value, int width) {
        String text = value == null ? "" : value;
        if (text.length() <= width) {
            return text;
        }
        return text.substring(0, Math.max(0, width - 1)) + "…";
    }
}
