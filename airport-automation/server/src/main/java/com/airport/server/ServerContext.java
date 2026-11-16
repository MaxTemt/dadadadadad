package com.airport.server;

import com.airport.common.exception.DataAccessException;
import com.airport.common.net.Command;
import com.airport.server.backup.BackupService;
import com.airport.server.config.ServerConfig;
import com.airport.server.dao.DaoFactory;
import com.airport.server.db.ConnectionPool;
import com.airport.server.handler.AdminCommands;
import com.airport.server.handler.AnalyticsCommands;
import com.airport.server.handler.AuthCommands;
import com.airport.server.handler.BookingCommands;
import com.airport.server.handler.FlightCommands;
import com.airport.server.handler.PassengerCommands;
import com.airport.server.handler.ReportCommands;
import com.airport.server.net.CommandRegistry;
import com.airport.server.report.ReportService;
import com.airport.server.service.AirlineService;
import com.airport.server.service.AnalyticsService;
import com.airport.server.service.AuthService;
import com.airport.server.service.BookingService;
import com.airport.server.service.FlightService;
import com.airport.server.service.PassengerService;

/**
 * Собирает все компоненты серверного приложения: пул соединений, DAO, сервисы и команды.
 */
public final class ServerContext implements AutoCloseable {

    private final ConnectionPool connectionPool;
    private final CommandRegistry commandRegistry;

    private ServerContext(ConnectionPool connectionPool, CommandRegistry commandRegistry) {
        this.connectionPool = connectionPool;
        this.commandRegistry = commandRegistry;
    }

    public static ServerContext create(ServerConfig config) throws DataAccessException {
        ConnectionPool pool = new ConnectionPool(config);
        try {
            DaoFactory dao = new DaoFactory(pool);

            AuthService authService = new AuthService(dao.users());
            AirlineService airlineService = new AirlineService(dao.airlines());
            PassengerService passengerService = new PassengerService(dao.passengers());
            FlightService flightService = new FlightService(dao.flights(), dao.airlines(), dao.bookings());
            BookingService bookingService = new BookingService(dao.bookings(), dao.flights(), dao.passengers());
            AnalyticsService analyticsService = new AnalyticsService(dao.analytics());
            ReportService reportService = new ReportService(dao.flights(), dao.bookings(),
                    config.getReportsDir());
            BackupService backupService = new BackupService(pool, config.getBackupsDir());

            CommandRegistry registry = new CommandRegistry();
            registry.register(Command.PING, new AuthCommands.Ping());
            registry.register(Command.LOGIN, new AuthCommands.Login(authService));
            registry.register(Command.LOGOUT, new AuthCommands.Logout());
            registry.register(Command.CURRENT_USER, new AuthCommands.CurrentUser());

            registry.register(Command.FLIGHT_LIST, new FlightCommands.List(flightService));
            registry.register(Command.FLIGHT_GET, new FlightCommands.Get(flightService));
            registry.register(Command.FLIGHT_SAVE, new FlightCommands.Save(flightService));
            registry.register(Command.FLIGHT_DELETE, new FlightCommands.Delete(flightService));
            registry.register(Command.AIRLINE_LIST, new FlightCommands.AirlineList(airlineService));
            registry.register(Command.AIRLINE_SAVE, new FlightCommands.AirlineSave(airlineService));
            registry.register(Command.AIRLINE_DELETE, new FlightCommands.AirlineDelete(airlineService));

            registry.register(Command.PASSENGER_LIST, new PassengerCommands.List(passengerService));
            registry.register(Command.PASSENGER_SAVE, new PassengerCommands.Save(passengerService));
            registry.register(Command.PASSENGER_DELETE, new PassengerCommands.Delete(passengerService));

            registry.register(Command.BOOKING_LIST, new BookingCommands.List(bookingService));
            registry.register(Command.BOOKING_SAVE, new BookingCommands.Save(bookingService));
            registry.register(Command.BOOKING_DELETE, new BookingCommands.Delete(bookingService));
            registry.register(Command.BOOKING_CHECK_IN, new BookingCommands.CheckIn(bookingService));
            registry.register(Command.BOOKING_CANCEL, new BookingCommands.Cancel(bookingService));

            registry.register(Command.USER_LIST, new AdminCommands.UserList(authService));
            registry.register(Command.USER_SAVE, new AdminCommands.UserSave(authService));
            registry.register(Command.USER_DELETE, new AdminCommands.UserDelete(authService));
            registry.register(Command.BACKUP_CREATE, new AdminCommands.BackupCreate(backupService));
            registry.register(Command.BACKUP_LIST, new AdminCommands.BackupList(backupService));
            registry.register(Command.BACKUP_RESTORE, new AdminCommands.BackupRestore(backupService));

            registry.register(Command.DASHBOARD, new AnalyticsCommands.Dashboard(analyticsService));
            registry.register(Command.CHART_OCCUPANCY, new AnalyticsCommands.Occupancy(analyticsService));
            registry.register(Command.CHART_STATUSES, new AnalyticsCommands.Statuses(analyticsService));
            registry.register(Command.CHART_REVENUE, new AnalyticsCommands.Revenue(analyticsService));

            registry.register(Command.REPORT_FLIGHTS, new ReportCommands.Flights(reportService));
            registry.register(Command.REPORT_BOOKINGS, new ReportCommands.Bookings(reportService));
            registry.register(Command.REPORT_LIST, new ReportCommands.List(reportService));
            registry.register(Command.REPORT_READ, new ReportCommands.Read(reportService));

            return new ServerContext(pool, registry);
        } catch (RuntimeException exception) {
            pool.close();
            throw exception;
        }
    }

    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    @Override
    public void close() {
        connectionPool.close();
    }
}
