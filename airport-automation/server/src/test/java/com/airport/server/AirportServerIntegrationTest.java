package com.airport.server;

import com.airport.client.service.AirportClientService;
import com.airport.common.dto.BackupInfo;
import com.airport.common.dto.ChartData;
import com.airport.common.dto.DashboardStats;
import com.airport.common.dto.ReportInfo;
import com.airport.common.dto.ReportResult;
import com.airport.common.exception.AirportException;
import com.airport.common.model.Booking;
import com.airport.common.model.BookingStatus;
import com.airport.common.model.Flight;
import com.airport.common.model.FlightStatus;
import com.airport.common.model.Passenger;
import com.airport.common.model.Role;
import com.airport.common.model.TicketClass;
import com.airport.common.model.User;
import com.airport.server.config.ServerConfig;
import com.airport.server.net.AirportServer;
import org.h2.tools.RunScript;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Интеграционные тесты: настоящий TCP-сервер, H2 в режиме MySQL и клиентский сервис.
 */
class AirportServerIntegrationTest {

    @TempDir
    Path tempDir;

    private final List<AirportClientService> clients =
            Collections.synchronizedList(new ArrayList<>());

    private ServerContext context;
    private AirportServer server;
    private int port;
    private Path reportsDir;
    private Path backupsDir;

    @BeforeEach
    void setUp() throws Exception {
        String database = "airport_" + UUID.randomUUID().toString().replace("-", "");
        String url = "jdbc:h2:mem:" + database
                + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
             InputStreamReader reader = new InputStreamReader(
                     getClass().getResourceAsStream("/schema-h2.sql"), StandardCharsets.UTF_8)) {
            RunScript.execute(connection, reader);
        }

        reportsDir = tempDir.resolve("reports");
        backupsDir = tempDir.resolve("backups");
        ServerConfig config = new ServerConfig.Builder()
                .host("127.0.0.1")
                .port(0)
                .maxThreads(8)
                .dbUrl(url)
                .dbUser("sa")
                .dbPassword("")
                .poolSize(4)
                .poolTimeoutMs(5_000)
                .reportsDir(reportsDir.toString())
                .backupsDir(backupsDir.toString())
                .build();
        context = ServerContext.create(config);
        server = new AirportServer(config, context.getCommandRegistry());
        server.start();
        port = server.getPort();
    }

    @AfterEach
    void tearDown() {
        clients.forEach(AirportClientService::disconnect);
        clients.clear();
        if (server != null) {
            server.close();
        }
        if (context != null) {
            context.close();
        }
    }

    @Test
    void loginAndRoleBasedAccess() throws Exception {
        AirportClientService admin = client();
        User adminUser = admin.login("admin", "admin123");
        assertEquals("admin", adminUser.getUsername());
        assertEquals(Role.ADMIN, adminUser.getRole());
        assertTrue(adminUser.isAdmin());
        assertNull(adminUser.getPasswordHash(), "Хеш пароля не должен передаваться клиенту");

        AirportClientService agent = client();
        User agentUser = agent.login("agent", "agent123");
        assertEquals(Role.USER, agentUser.getRole());
        assertFalse(agentUser.isAdmin());

        AirportClientService wrong = client();
        AirportException error = assertThrows(AirportException.class,
                () -> wrong.login("admin", "wrong-password"));
        assertEquals("AUTHENTICATION", error.getCode());

        assertThrows(AirportException.class, () -> agent.listUsers());
        assertThrows(AirportException.class, () -> agent.deleteFlight(1));
        assertThrows(AirportException.class, () -> agent.deletePassenger(1));
        assertThrows(AirportException.class, () -> agent.createBackup());
    }

    @Test
    void pingWorksWithoutAuthentication() throws Exception {
        AirportClientService service = client();
        assertEquals("PONG", service.ping());
    }

    @Test
    void flightPassengerAndBookingCrud() throws Exception {
        AirportClientService admin = client();
        admin.login("admin", "admin123");

        Passenger passenger = new Passenger();
        passenger.setFirstName("Тест");
        passenger.setLastName("Пассажиров");
        passenger.setPassportNumber("9000 123456");
        passenger.setEmail("test.passenger@example.com");
        passenger.setPhone("+7 (900) 000-00-00");
        passenger.setDateOfBirth(LocalDate.of(1991, 3, 15));
        passenger.setFrequentFlyerLevel("SILVER");
        Passenger savedPassenger = admin.savePassenger(passenger);
        assertNotNull(savedPassenger.getId());

        LocalDateTime departure = LocalDateTime.now().plusDays(10).withMinute(0).withSecond(0)
                .withNano(0);
        Flight flight = new Flight();
        flight.setFlightNumber("TS9001");
        flight.setAirlineId(1L);
        flight.setOriginCity("Москва");
        flight.setDestinationCity("Самара");
        flight.setDepartureTime(departure);
        flight.setArrivalTime(departure.plusHours(2));
        flight.setGate("E5");
        flight.setAircraftType("Airbus A320");
        flight.setTotalSeats(120);
        flight.setStatus(FlightStatus.SCHEDULED);
        flight.setBasePrice(new BigDecimal("5000.00"));
        Flight savedFlight = admin.saveFlight(flight);
        assertNotNull(savedFlight.getId());

        Booking booking = new Booking(savedFlight.getId(), savedPassenger.getId(), "1A",
                TicketClass.ECONOMY);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setBookingTime(LocalDateTime.now());
        Booking savedBooking = admin.saveBooking(booking);
        assertNotNull(savedBooking.getId());
        assertEquals(new BigDecimal("5000.00"), savedBooking.getPrice());
        assertEquals("TS9001", savedBooking.getFlightNumber());
        assertTrue(savedBooking.getPassengerName().contains("Пассажиров"));

        Booking loaded = admin.listBookings("TS9001", null).get(0);
        loaded.setStatus(null);
        Booking resaved = admin.saveBooking(loaded);
        assertEquals(BookingStatus.CONFIRMED, resaved.getStatus(),
                "При отсутствии статуса существующая бронь не должна сбрасываться");

        Booking checkedIn = admin.checkIn(savedBooking.getId());
        assertEquals(BookingStatus.CHECKED_IN, checkedIn.getStatus());

        Booking cancelled = admin.cancelBooking(savedBooking.getId());
        assertEquals(BookingStatus.CANCELLED, cancelled.getStatus());

        Booking second = new Booking(savedFlight.getId(), savedPassenger.getId(), "1A",
                TicketClass.BUSINESS);
        second.setStatus(BookingStatus.CONFIRMED);
        Booking secondSaved = admin.saveBooking(second);
        assertEquals(new BigDecimal("9000.00"), secondSaved.getPrice());

        admin.deleteBooking(secondSaved.getId());
        admin.deleteBooking(savedBooking.getId());
        admin.deleteFlight(savedFlight.getId());
        admin.deletePassenger(savedPassenger.getId());

        assertTrue(admin.listPassengers("9000 123456").isEmpty());
    }

    @Test
    void analyticsAndReportsAreProducedOnServer() throws Exception {
        AirportClientService admin = client();
        admin.login("admin", "admin123");

        DashboardStats stats = admin.dashboard();
        assertEquals(8, stats.getTotalFlights());
        assertEquals(5, stats.getTotalPassengers());
        assertTrue(stats.getActiveBookings() >= 8);
        assertTrue(stats.getRevenue() > 0);
        assertTrue(stats.getAverageOccupancy() > 0);

        ChartData occupancy = admin.occupancyChart();
        assertFalse(occupancy.isEmpty());
        assertEquals(7, occupancy.getLabels().size(), "Отменённый рейс не должен попадать в график");

        ChartData statuses = admin.statusChart();
        assertFalse(statuses.isEmpty());
        assertTrue(statuses.getTotal() >= 9);

        ChartData revenue = admin.revenueChart();
        assertFalse(revenue.isEmpty());

        ReportResult flights = admin.generateFlightsReport();
        assertTrue(flights.getFileName().startsWith("flights-report-"));
        assertTrue(flights.getContent().contains("ОТЧЁТ ПО РЕЙСАМ АЭРОПОРТА"));
        assertTrue(flights.getContent().contains("SU1234"));
        assertTrue(Files.isRegularFile(reportsDir.resolve(flights.getFileName())));

        ReportResult bookings = admin.generateBookingsReport();
        assertTrue(bookings.getContent().contains("ОТЧЁТ ПО БРОНИРОВАНИЯМ"));
        assertTrue(bookings.getContent().contains("Иванов"));

        List<ReportInfo> reports = admin.listReports();
        assertEquals(2, reports.size());
        ReportInfo info = reports.stream()
                .filter(report -> report.getFileName().equals(flights.getFileName()))
                .findFirst().orElseThrow();
        assertEquals(Files.size(reportsDir.resolve(info.getFileName())), info.getSizeBytes());

        ReportResult read = admin.readReport(flights.getFileName());
        assertEquals(flights.getContent(), read.getContent());
    }

    @Test
    void backupAndRestoreReplacesData() throws Exception {
        AirportClientService admin = client();
        admin.login("admin", "admin123");

        Passenger passenger = new Passenger();
        passenger.setFirstName("Резерв");
        passenger.setLastName("Копиров");
        passenger.setPassportNumber("9100 555555");
        passenger.setDateOfBirth(LocalDate.of(1988, 7, 7));
        passenger = admin.savePassenger(passenger);
        assertFalse(admin.listPassengers("9100 555555").isEmpty());

        BackupInfo backup = admin.createBackup();
        assertTrue(backup.getFileName().endsWith(".xml"));
        assertTrue(Files.isRegularFile(backupsDir.resolve(backup.getFileName())));
        assertTrue(admin.listBackups().stream()
                .anyMatch(info -> info.getFileName().equals(backup.getFileName())));

        admin.deletePassenger(passenger.getId());
        assertTrue(admin.listPassengers("9100 555555").isEmpty());

        String message = admin.restoreBackup(backup.getFileName());
        assertTrue(message.contains("Восстановлено"));
        assertFalse(admin.listPassengers("9100 555555").isEmpty(),
                "Пассажир должен вернуться после восстановления копии");
    }

    @Test
    void manyClientsAreHandledConcurrently() throws Exception {
        int count = 8;
        ExecutorService executor = Executors.newFixedThreadPool(count);
        try {
            List<Callable<String>> tasks = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                tasks.add(() -> {
                    AirportClientService service = client();
                    service.login("agent", "agent123");
                    List<Flight> flights = service.listFlights(null, null, null);
                    assertFalse(flights.isEmpty());
                    return flights.get(0).getFlightNumber();
                });
            }
            List<Future<String>> results = executor.invokeAll(tasks, 30, TimeUnit.SECONDS);
            for (Future<String> result : results) {
                assertFalse(result.isCancelled());
                assertEquals("DP405", result.get());
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private AirportClientService client() throws AirportException {
        AirportClientService service = new AirportClientService(
                new com.airport.client.net.ServerConnection());
        service.connect("127.0.0.1", port);
        clients.add(service);
        return service;
    }
}
