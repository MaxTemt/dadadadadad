package com.airport.server.handler;

import com.airport.common.model.User;
import com.airport.common.net.Request;
import com.airport.common.net.Response;
import com.airport.server.net.CommandHandler;
import com.airport.server.net.SessionContext;
import com.airport.server.service.AuthService;

/**
 * Команды аутентификации.
 */
public final class AuthCommands {

    private AuthCommands() {
    }

    public static final class Ping implements CommandHandler {
        @Override
        public Response handle(Request request, SessionContext context) {
            return Response.ok("PONG");
        }
    }

    public static final class Login implements CommandHandler {
        private final AuthService authService;

        public Login(AuthService authService) {
            this.authService = authService;
        }

        @Override
        public Response handle(Request request, SessionContext context) throws Exception {
            User user = authService.login(request.getString("username"), request.getString("password"));
            context.login(user);
            return Response.ok("Добро пожаловать, " + user.getFullName(), user);
        }
    }

    public static final class Logout implements CommandHandler {
        @Override
        public Response handle(Request request, SessionContext context) {
            context.logout();
            return Response.ok("Вы вышли из системы");
        }
    }

    public static final class CurrentUser implements CommandHandler {
        @Override
        public Response handle(Request request, SessionContext context) throws Exception {
            return Response.ok(context.requireAuthenticated());
        }
    }
}
