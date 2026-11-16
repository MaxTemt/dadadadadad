package com.airport.server.handler;

import com.airport.common.model.Booking;
import com.airport.common.model.BookingStatus;
import com.airport.common.net.Request;
import com.airport.common.net.Response;
import com.airport.server.dao.BookingFilter;
import com.airport.server.net.CommandHandler;
import com.airport.server.net.SessionContext;
import com.airport.server.service.BookingService;

/**
 * Команды работы с бронированиями.
 */
public final class BookingCommands {

    private BookingCommands() {
    }

    public static final class List implements CommandHandler {
        private final BookingService service;

        public List(BookingService service) {
            this.service = service;
        }

        @Override
        public Response handle(Request request, SessionContext context) throws Exception {
            context.requireAuthenticated();
            BookingStatus status = request.has("status")
                    ? BookingStatus.fromString(request.getString("status")) : null;
            BookingFilter filter = new BookingFilter(request.getLong("flightId"),
                    request.getLong("passengerId"), status, request.getString("search"));
            return Response.ok(service.search(filter));
        }
    }

    public static final class Save implements CommandHandler {
        private final BookingService service;

        public Save(BookingService service) {
            this.service = service;
        }

        @Override
        public Response handle(Request request, SessionContext context) throws Exception {
            Booking booking = (Booking) request.get("booking");
            long actorId = context.getUserId();
            return Response.ok("Бронирование сохранено", service.save(booking, actorId));
        }
    }

    public static final class Delete implements CommandHandler {
        private final BookingService service;

        public Delete(BookingService service) {
            this.service = service;
        }

        @Override
        public Response handle(Request request, SessionContext context) throws Exception {
            context.requireAdmin();
            service.delete(request.getLong("id", 0));
            return Response.ok("Бронирование удалено");
        }
    }

    public static final class CheckIn implements CommandHandler {
        private final BookingService service;

        public CheckIn(BookingService service) {
            this.service = service;
        }

        @Override
        public Response handle(Request request, SessionContext context) throws Exception {
            context.requireAuthenticated();
            return Response.ok("Регистрация выполнена",
                    service.checkIn(request.getLong("id", 0)));
        }
    }

    public static final class Cancel implements CommandHandler {
        private final BookingService service;

        public Cancel(BookingService service) {
            this.service = service;
        }

        @Override
        public Response handle(Request request, SessionContext context) throws Exception {
            context.requireAuthenticated();
            return Response.ok("Бронирование отменено",
                    service.cancel(request.getLong("id", 0)));
        }
    }
}
