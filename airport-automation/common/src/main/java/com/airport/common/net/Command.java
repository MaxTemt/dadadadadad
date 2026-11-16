package com.airport.common.net;

/**
 * Имена команд протокола клиент-серверного взаимодействия.
 */
public final class Command {

    private Command() {
    }

    public static final String PING = "PING";
    public static final String LOGIN = "LOGIN";
    public static final String LOGOUT = "LOGOUT";
    public static final String CURRENT_USER = "CURRENT_USER";

    public static final String FLIGHT_LIST = "FLIGHT_LIST";
    public static final String FLIGHT_GET = "FLIGHT_GET";
    public static final String FLIGHT_SAVE = "FLIGHT_SAVE";
    public static final String FLIGHT_DELETE = "FLIGHT_DELETE";

    public static final String AIRLINE_LIST = "AIRLINE_LIST";
    public static final String AIRLINE_SAVE = "AIRLINE_SAVE";
    public static final String AIRLINE_DELETE = "AIRLINE_DELETE";

    public static final String PASSENGER_LIST = "PASSENGER_LIST";
    public static final String PASSENGER_SAVE = "PASSENGER_SAVE";
    public static final String PASSENGER_DELETE = "PASSENGER_DELETE";

    public static final String BOOKING_LIST = "BOOKING_LIST";
    public static final String BOOKING_SAVE = "BOOKING_SAVE";
    public static final String BOOKING_DELETE = "BOOKING_DELETE";
    public static final String BOOKING_CHECK_IN = "BOOKING_CHECK_IN";
    public static final String BOOKING_CANCEL = "BOOKING_CANCEL";

    public static final String USER_LIST = "USER_LIST";
    public static final String USER_SAVE = "USER_SAVE";
    public static final String USER_DELETE = "USER_DELETE";

    public static final String DASHBOARD = "DASHBOARD";
    public static final String CHART_OCCUPANCY = "CHART_OCCUPANCY";
    public static final String CHART_STATUSES = "CHART_STATUSES";
    public static final String CHART_REVENUE = "CHART_REVENUE";

    public static final String REPORT_FLIGHTS = "REPORT_FLIGHTS";
    public static final String REPORT_BOOKINGS = "REPORT_BOOKINGS";
    public static final String REPORT_LIST = "REPORT_LIST";
    public static final String REPORT_READ = "REPORT_READ";

    public static final String BACKUP_CREATE = "BACKUP_CREATE";
    public static final String BACKUP_LIST = "BACKUP_LIST";
    public static final String BACKUP_RESTORE = "BACKUP_RESTORE";
}
