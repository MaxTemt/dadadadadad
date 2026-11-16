package com.airport.server.net;

import com.airport.common.exception.AuthenticationException;
import com.airport.common.exception.AuthorizationException;
import com.airport.common.model.User;

import java.net.Socket;
import java.time.LocalDateTime;

/**
 * Состояние одного клиентского подключения: аутентифицированный пользователь,
 * время подключения и удалённый адрес.
 */
public final class SessionContext {

    private final Socket socket;
    private final String remoteAddress;
    private final LocalDateTime connectedAt = LocalDateTime.now();
    private volatile User user;

    public SessionContext(Socket socket) {
        this.socket = socket;
        this.remoteAddress = socket.getRemoteSocketAddress() == null
                ? "unknown" : socket.getRemoteSocketAddress().toString();
    }

    public void login(User user) {
        this.user = user;
    }

    public void logout() {
        this.user = null;
    }

    public User getUser() {
        return user;
    }

    public boolean isAuthenticated() {
        return user != null;
    }

    public User requireAuthenticated() throws AuthenticationException {
        if (user == null) {
            throw new AuthenticationException("Требуется авторизация");
        }
        return user;
    }

    public User requireAdmin() throws AuthenticationException, AuthorizationException {
        User authenticated = requireAuthenticated();
        if (!authenticated.isAdmin()) {
            throw new AuthorizationException("Операция доступна только администратору");
        }
        return authenticated;
    }

    public long getUserId() throws AuthenticationException {
        return requireAuthenticated().getId();
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public LocalDateTime getConnectedAt() {
        return connectedAt;
    }

    public Socket getSocket() {
        return socket;
    }
}
