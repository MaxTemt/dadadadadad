package com.airport.server.handler;

import com.airport.common.model.User;
import com.airport.common.net.Request;
import com.airport.common.net.Response;
import com.airport.server.net.CommandHandler;
import com.airport.server.net.SessionContext;
import com.airport.server.report.ReportService;

/**
 * Команды формирования и чтения текстовых отчётов.
 */
public final class ReportCommands {

    private ReportCommands() {
    }

    public static final class Flights implements CommandHandler {
        private final ReportService service;

        public Flights(ReportService service) {
            this.service = service;
        }

        @Override
        public Response handle(Request request, SessionContext context) throws Exception {
            User user = context.requireAuthenticated();
            return Response.ok(service.generateFlightsReport(user.getFullName()));
        }
    }

    public static final class Bookings implements CommandHandler {
        private final ReportService service;

        public Bookings(ReportService service) {
            this.service = service;
        }

        @Override
        public Response handle(Request request, SessionContext context) throws Exception {
            User user = context.requireAuthenticated();
            return Response.ok(service.generateBookingsReport(user.getFullName()));
        }
    }

    public static final class List implements CommandHandler {
        private final ReportService service;

        public List(ReportService service) {
            this.service = service;
        }

        @Override
        public Response handle(Request request, SessionContext context) throws Exception {
            context.requireAuthenticated();
            return Response.ok(service.listReports());
        }
    }

    public static final class Read implements CommandHandler {
        private final ReportService service;

        public Read(ReportService service) {
            this.service = service;
        }

        @Override
        public Response handle(Request request, SessionContext context) throws Exception {
            context.requireAuthenticated();
            return Response.ok(service.readReport(request.getString("fileName")));
        }
    }
}
