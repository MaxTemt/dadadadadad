package com.airport.server.handler;

import com.airport.common.net.Request;
import com.airport.common.net.Response;
import com.airport.server.net.CommandHandler;
import com.airport.server.net.SessionContext;
import com.airport.server.service.AnalyticsService;

/**
 * Команды аналитики.
 */
public final class AnalyticsCommands {

    private AnalyticsCommands() {
    }

    public static final class Dashboard implements CommandHandler {
        private final AnalyticsService service;

        public Dashboard(AnalyticsService service) {
            this.service = service;
        }

        @Override
        public Response handle(Request request, SessionContext context) throws Exception {
            context.requireAuthenticated();
            return Response.ok(service.dashboard());
        }
    }

    public static final class Occupancy implements CommandHandler {
        private final AnalyticsService service;

        public Occupancy(AnalyticsService service) {
            this.service = service;
        }

        @Override
        public Response handle(Request request, SessionContext context) throws Exception {
            context.requireAuthenticated();
            return Response.ok(service.occupancyChart());
        }
    }

    public static final class Statuses implements CommandHandler {
        private final AnalyticsService service;

        public Statuses(AnalyticsService service) {
            this.service = service;
        }

        @Override
        public Response handle(Request request, SessionContext context) throws Exception {
            context.requireAuthenticated();
            return Response.ok(service.statusChart());
        }
    }

    public static final class Revenue implements CommandHandler {
        private final AnalyticsService service;

        public Revenue(AnalyticsService service) {
            this.service = service;
        }

        @Override
        public Response handle(Request request, SessionContext context) throws Exception {
            context.requireAuthenticated();
            return Response.ok(service.revenueChart());
        }
    }
}
