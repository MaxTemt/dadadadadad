package com.airport.server.handler;

import com.airport.common.model.User;
import com.airport.common.net.Request;
import com.airport.common.net.Response;
import com.airport.server.backup.BackupService;
import com.airport.server.net.CommandHandler;
import com.airport.server.net.SessionContext;
import com.airport.server.service.AuthService;

/**
 * Административные команды: пользователи и резервное копирование.
 */
public final class AdminCommands {

    private AdminCommands() {
    }

    public static final class UserList implements CommandHandler {
        private final AuthService service;

        public UserList(AuthService service) {
            this.service = service;
        }

        @Override
        public Response handle(Request request, SessionContext context) throws Exception {
            context.requireAdmin();
            return Response.ok(service.listUsers());
        }
    }

    public static final class UserSave implements CommandHandler {
        private final AuthService service;

        public UserSave(AuthService service) {
            this.service = service;
        }

        @Override
        public Response handle(Request request, SessionContext context) throws Exception {
            context.requireAdmin();
            User user = (User) request.get("user");
            String password = request.getString("password");
            return Response.ok("Пользователь сохранён", service.saveUser(user, password));
        }
    }

    public static final class UserDelete implements CommandHandler {
        private final AuthService service;

        public UserDelete(AuthService service) {
            this.service = service;
        }

        @Override
        public Response handle(Request request, SessionContext context) throws Exception {
            User admin = context.requireAdmin();
            service.deleteUser(request.getLong("id", 0), admin.getId());
            return Response.ok("Пользователь удалён");
        }
    }

    public static final class BackupCreate implements CommandHandler {
        private final BackupService service;

        public BackupCreate(BackupService service) {
            this.service = service;
        }

        @Override
        public Response handle(Request request, SessionContext context) throws Exception {
            User admin = context.requireAdmin();
            return Response.ok("Резервная копия создана",
                    service.createBackup(admin.getFullName()));
        }
    }

    public static final class BackupList implements CommandHandler {
        private final BackupService service;

        public BackupList(BackupService service) {
            this.service = service;
        }

        @Override
        public Response handle(Request request, SessionContext context) throws Exception {
            context.requireAdmin();
            return Response.ok(service.listBackups());
        }
    }

    public static final class BackupRestore implements CommandHandler {
        private final BackupService service;

        public BackupRestore(BackupService service) {
            this.service = service;
        }

        @Override
        public Response handle(Request request, SessionContext context) throws Exception {
            context.requireAdmin();
            String message = service.restoreBackup(request.getString("fileName"));
            return Response.ok(message);
        }
    }
}
