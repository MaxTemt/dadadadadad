package com.airport.server.handler;

import com.airport.common.model.Passenger;
import com.airport.common.net.Request;
import com.airport.common.net.Response;
import com.airport.server.net.CommandHandler;
import com.airport.server.net.SessionContext;
import com.airport.server.service.PassengerService;

/**
 * Команды работы с пассажирами.
 */
public final class PassengerCommands {

    private PassengerCommands() {
    }

    public static final class List implements CommandHandler {
        private final PassengerService service;

        public List(PassengerService service) {
            this.service = service;
        }

        @Override
        public Response handle(Request request, SessionContext context) throws Exception {
            context.requireAuthenticated();
            return Response.ok(service.search(request.getString("search")));
        }
    }

    public static final class Save implements CommandHandler {
        private final PassengerService service;

        public Save(PassengerService service) {
            this.service = service;
        }

        @Override
        public Response handle(Request request, SessionContext context) throws Exception {
            context.requireAuthenticated();
            Passenger passenger = (Passenger) request.get("passenger");
            return Response.ok("Пассажир сохранён", service.save(passenger));
        }
    }

    public static final class Delete implements CommandHandler {
        private final PassengerService service;

        public Delete(PassengerService service) {
            this.service = service;
        }

        @Override
        public Response handle(Request request, SessionContext context) throws Exception {
            context.requireAdmin();
            service.delete(request.getLong("id", 0));
            return Response.ok("Пассажир удалён");
        }
    }
}
