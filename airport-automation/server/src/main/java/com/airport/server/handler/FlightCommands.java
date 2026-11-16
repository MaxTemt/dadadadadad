package com.airport.server.handler;

import com.airport.common.model.Airline;
import com.airport.common.model.Flight;
import com.airport.common.model.FlightStatus;
import com.airport.common.net.Request;
import com.airport.common.net.Response;
import com.airport.server.dao.FlightFilter;
import com.airport.server.net.CommandHandler;
import com.airport.server.net.SessionContext;
import com.airport.server.service.AirlineService;
import com.airport.server.service.FlightService;

/**
 * Команды работы с рейсами и авиакомпаниями.
 */
public final class FlightCommands {

    private FlightCommands() {
    }

    public static final class List implements CommandHandler {
        private final FlightService service;

        public List(FlightService service) {
            this.service = service;
        }

        @Override
        public Response handle(Request request, SessionContext context) throws Exception {
            context.requireAuthenticated();
            FlightStatus status = request.has("status")
                    ? FlightStatus.fromString(request.getString("status")) : null;
            FlightFilter filter = new FlightFilter(request.getString("search"), status,
                    request.getDate("date"));
            return Response.ok(service.search(filter));
        }
    }

    public static final class Get implements CommandHandler {
        private final FlightService service;

        public Get(FlightService service) {
            this.service = service;
        }

        @Override
        public Response handle(Request request, SessionContext context) throws Exception {
            context.requireAuthenticated();
            return Response.ok(service.get(request.getLong("id", 0)));
        }
    }

    public static final class Save implements CommandHandler {
        private final FlightService service;

        public Save(FlightService service) {
            this.service = service;
        }

        @Override
        public Response handle(Request request, SessionContext context) throws Exception {
            context.requireAdmin();
            Flight flight = (Flight) request.get("flight");
            Flight saved = service.save(flight);
            return Response.ok("Рейс сохранён", saved);
        }
    }

    public static final class Delete implements CommandHandler {
        private final FlightService service;

        public Delete(FlightService service) {
            this.service = service;
        }

        @Override
        public Response handle(Request request, SessionContext context) throws Exception {
            context.requireAdmin();
            service.delete(request.getLong("id", 0));
            return Response.ok("Рейс удалён");
        }
    }

    public static final class AirlineList implements CommandHandler {
        private final AirlineService service;

        public AirlineList(AirlineService service) {
            this.service = service;
        }

        @Override
        public Response handle(Request request, SessionContext context) throws Exception {
            context.requireAuthenticated();
            return Response.ok(service.list());
        }
    }

    public static final class AirlineSave implements CommandHandler {
        private final AirlineService service;

        public AirlineSave(AirlineService service) {
            this.service = service;
        }

        @Override
        public Response handle(Request request, SessionContext context) throws Exception {
            context.requireAdmin();
            Airline airline = (Airline) request.get("airline");
            return Response.ok("Авиакомпания сохранена", service.save(airline));
        }
    }

    public static final class AirlineDelete implements CommandHandler {
        private final AirlineService service;

        public AirlineDelete(AirlineService service) {
            this.service = service;
        }

        @Override
        public Response handle(Request request, SessionContext context) throws Exception {
            context.requireAdmin();
            service.delete(request.getLong("id", 0));
            return Response.ok("Авиакомпания удалена");
        }
    }
}
