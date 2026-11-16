package com.airport.server.dao;

import com.airport.common.dto.ChartData;
import com.airport.common.dto.DashboardStats;
import com.airport.common.exception.DataAccessException;
import com.airport.common.model.BookingStatus;
import com.airport.server.db.ConnectionPool;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Запросы аналитики и агрегированных показателей.
 */
public class AnalyticsDao extends AbstractJdbcDao {

    public AnalyticsDao(ConnectionPool pool) {
        super(pool);
    }

    public DashboardStats dashboard() throws DataAccessException {
        int totalFlights = (int) queryLong("SELECT COUNT(*) FROM flights");
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        int todayFlights = (int) queryLong(
                "SELECT COUNT(*) FROM flights WHERE departure_time >= ? AND departure_time < ?",
                start, end);
        int passengers = (int) queryLong("SELECT COUNT(*) FROM passengers");
        int activeBookings = (int) queryLong(
                "SELECT COUNT(*) FROM bookings WHERE booking_status <> 'CANCELLED'");
        double revenue = queryDouble(
                "SELECT COALESCE(SUM(price), 0) FROM bookings WHERE booking_status <> 'CANCELLED'");
        double occupancy = queryDouble("""
                SELECT COALESCE(SUM(booked) * 100.0 / NULLIF(SUM(total_seats), 0), 0)
                  FROM (SELECT f.id, f.total_seats, COUNT(b.id) AS booked
                          FROM flights f
                          LEFT JOIN bookings b ON b.flight_id = f.id
                               AND b.booking_status <> 'CANCELLED'
                         GROUP BY f.id, f.total_seats) t
                """);
        int cancelledFlights = (int) queryLong(
                "SELECT COUNT(*) FROM flights WHERE flight_status = 'CANCELLED'");
        return new DashboardStats(totalFlights, todayFlights, passengers, activeBookings,
                revenue, occupancy, cancelledFlights);
    }

    public ChartData occupancyByFlight() throws DataAccessException {
        ChartData chart = new ChartData("Загрузка рейсов", "%");
        String sql = """
                SELECT f.flight_number,
                       COALESCE(ROUND(COUNT(b.id) * 100.0 / NULLIF(f.total_seats, 0), 1), 0) AS occupancy
                  FROM flights f
                  LEFT JOIN bookings b ON b.flight_id = f.id
                       AND b.booking_status <> 'CANCELLED'
                 WHERE f.flight_status <> 'CANCELLED'
                 GROUP BY f.id, f.flight_number, f.departure_time, f.total_seats
                 ORDER BY f.departure_time
                """;
        pool.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    chart.add(resultSet.getString("flight_number"), resultSet.getDouble("occupancy"));
                }
            }
            return null;
        });
        return chart;
    }

    public ChartData bookingStatuses() throws DataAccessException {
        String sql = "SELECT booking_status, COUNT(*) AS total FROM bookings GROUP BY booking_status";
        Map<BookingStatus, Double> counts = new HashMap<>();
        pool.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    counts.put(BookingStatus.fromString(resultSet.getString("booking_status")),
                            resultSet.getDouble("total"));
                }
            }
            return null;
        });
        ChartData chart = new ChartData("Распределение бронирований по статусам", "броней");
        for (BookingStatus status : BookingStatus.values()) {
            double value = counts.getOrDefault(status, 0.0);
            if (value > 0) {
                chart.add(status.getTitle(), value);
            }
        }
        return chart;
    }

    public ChartData revenueByAirline() throws DataAccessException {
        String sql = """
                SELECT a.name, COALESCE(SUM(CASE WHEN b.booking_status <> 'CANCELLED'
                                                 THEN b.price ELSE 0 END), 0) AS revenue
                  FROM airlines a
                  LEFT JOIN flights f ON f.airline_id = a.id
                  LEFT JOIN bookings b ON b.flight_id = f.id
                 GROUP BY a.id, a.name
                HAVING COALESCE(SUM(CASE WHEN b.booking_status <> 'CANCELLED'
                                         THEN b.price ELSE 0 END), 0) > 0
                 ORDER BY revenue DESC
                """;
        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        pool.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    labels.add(resultSet.getString("name"));
                    values.add(resultSet.getDouble("revenue"));
                }
            }
            return null;
        });
        return new ChartData("Выручка по авиакомпаниям", "руб.", labels, values);
    }
}
