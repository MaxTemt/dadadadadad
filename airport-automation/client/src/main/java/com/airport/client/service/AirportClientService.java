package com.airport.client.service;

import com.airport.client.net.ServerConnection;
import com.airport.common.dto.BackupInfo;
import com.airport.common.dto.ChartData;
import com.airport.common.dto.DashboardStats;
import com.airport.common.dto.ReportInfo;
import com.airport.common.dto.ReportResult;
import com.airport.common.exception.AirportException;
import com.airport.common.model.Airline;
import com.airport.common.model.Booking;
import com.airport.common.model.BookingStatus;
import com.airport.common.model.Flight;
import com.airport.common.model.FlightStatus;
import com.airport.common.model.Passenger;
import com.airport.common.model.User;
import com.airport.common.net.Command;
import com.airport.common.net.Request;
import com.airport.common.net.Response;

import java.time.LocalDate;
import java.util.List;

/**
 * Клиентский фасад серверных команд. Скрывает детали протокола от интерфейса.
 */
public class AirportClientService {

    private final ServerConnection connection;
    private boolean authenticated;

    public AirportClientService(ServerConnection connection) {
        this.connection = connection;
    }

    public void connect(String host, int port) throws AirportException {
        connection.connect(host, port);
        authenticated = false;
    }

    public void disconnect() {
        try {
            if (connection.isConnected() && authenticated) {
                send(Request.of(Command.LOGOUT));
            }
        } catch (AirportException ignored) {
            // сервер мог уже закрыть соединение
        } finally {
            authenticated = false;
            connection.close();
        }
    }

    public String getRemote() {
        return connection.getRemote();
    }

    public User login(String username, String password) throws AirportException {
        Response response = send(Request.of(Command.LOGIN)
                .with("username", username)
                .with("password", password));
        User user = data(response);
        authenticated = true;
        return user;
    }

    public String ping() throws AirportException {
        return data(send(Request.of(Command.PING)));
    }

    public void logout() throws AirportException {
        send(Request.of(Command.LOGOUT));
        authenticated = false;
    }

    public List<Flight> listFlights(String search, FlightStatus status, LocalDate date)
            throws AirportException {
        Request request = Request.of(Command.FLIGHT_LIST)
                .with("search", search)
                .with("date", date == null ? null : date.toString());
        if (status != null) {
            request.with("status", status.name());
        }
        return data(send(request));
    }

    public List<Flight> listAllFlights() throws AirportException {
        return listFlights(null, null, null);
    }

    public Flight saveFlight(Flight flight) throws AirportException {
        return data(send(Request.of(Command.FLIGHT_SAVE).with("flight", flight)));
    }

    public void deleteFlight(long id) throws AirportException {
        send(Request.of(Command.FLIGHT_DELETE).with("id", id));
    }

    public List<Airline> listAirlines() throws AirportException {
        return data(send(Request.of(Command.AIRLINE_LIST)));
    }

    public Airline saveAirline(Airline airline) throws AirportException {
        return data(send(Request.of(Command.AIRLINE_SAVE).with("airline", airline)));
    }

    public void deleteAirline(long id) throws AirportException {
        send(Request.of(Command.AIRLINE_DELETE).with("id", id));
    }

    public List<Passenger> listPassengers(String search) throws AirportException {
        return data(send(Request.of(Command.PASSENGER_LIST).with("search", search)));
    }

    public Passenger savePassenger(Passenger passenger) throws AirportException {
        return data(send(Request.of(Command.PASSENGER_SAVE).with("passenger", passenger)));
    }

    public void deletePassenger(long id) throws AirportException {
        send(Request.of(Command.PASSENGER_DELETE).with("id", id));
    }

    public List<Booking> listBookings(String search, BookingStatus status) throws AirportException {
        Request request = Request.of(Command.BOOKING_LIST).with("search", search);
        if (status != null) {
            request.with("status", status.name());
        }
        return data(send(request));
    }

    public Booking saveBooking(Booking booking) throws AirportException {
        return data(send(Request.of(Command.BOOKING_SAVE).with("booking", booking)));
    }

    public void deleteBooking(long id) throws AirportException {
        send(Request.of(Command.BOOKING_DELETE).with("id", id));
    }

    public Booking checkIn(long id) throws AirportException {
        return data(send(Request.of(Command.BOOKING_CHECK_IN).with("id", id)));
    }

    public Booking cancelBooking(long id) throws AirportException {
        return data(send(Request.of(Command.BOOKING_CANCEL).with("id", id)));
    }

    public List<User> listUsers() throws AirportException {
        return data(send(Request.of(Command.USER_LIST)));
    }

    public User saveUser(User user, String password) throws AirportException {
        return data(send(Request.of(Command.USER_SAVE)
                .with("user", user)
                .with("password", password)));
    }

    public void deleteUser(long id) throws AirportException {
        send(Request.of(Command.USER_DELETE).with("id", id));
    }

    public DashboardStats dashboard() throws AirportException {
        return data(send(Request.of(Command.DASHBOARD)));
    }

    public ChartData occupancyChart() throws AirportException {
        return data(send(Request.of(Command.CHART_OCCUPANCY)));
    }

    public ChartData statusChart() throws AirportException {
        return data(send(Request.of(Command.CHART_STATUSES)));
    }

    public ChartData revenueChart() throws AirportException {
        return data(send(Request.of(Command.CHART_REVENUE)));
    }

    public ReportResult generateFlightsReport() throws AirportException {
        return data(send(Request.of(Command.REPORT_FLIGHTS)));
    }

    public ReportResult generateBookingsReport() throws AirportException {
        return data(send(Request.of(Command.REPORT_BOOKINGS)));
    }

    public List<ReportInfo> listReports() throws AirportException {
        return data(send(Request.of(Command.REPORT_LIST)));
    }

    public ReportResult readReport(String fileName) throws AirportException {
        return data(send(Request.of(Command.REPORT_READ).with("fileName", fileName)));
    }

    public BackupInfo createBackup() throws AirportException {
        return data(send(Request.of(Command.BACKUP_CREATE)));
    }

    public List<BackupInfo> listBackups() throws AirportException {
        return data(send(Request.of(Command.BACKUP_LIST)));
    }

    public String restoreBackup(String fileName) throws AirportException {
        return data(send(Request.of(Command.BACKUP_RESTORE).with("fileName", fileName)));
    }

    private Response send(Request request) throws AirportException {
        Response response = connection.send(request);
        if (!response.isSuccess()) {
            throw new AirportException(response.getErrorCode(), response.getMessage());
        }
        return response;
    }

    @SuppressWarnings("unchecked")
    private <T> T data(Response response) throws AirportException {
        if (!response.isSuccess()) {
            throw new AirportException(response.getErrorCode(), response.getMessage());
        }
        return (T) response.getData();
    }
}
