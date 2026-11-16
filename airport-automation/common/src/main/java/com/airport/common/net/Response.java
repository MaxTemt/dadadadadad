package com.airport.common.net;

import java.io.Serializable;

/**
 * Ответ сервера. Содержит признак успеха, сообщение, код ошибки и полезные данные.
 */
public class Response implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;
    private final String errorCode;
    private final Object data;

    private Response(boolean success, String message, String errorCode, Object data) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
        this.data = data;
    }

    public static Response ok() {
        return new Response(true, null, null, null);
    }

    public static Response ok(Object data) {
        return new Response(true, null, null, data);
    }

    public static Response ok(String message, Object data) {
        return new Response(true, message, null, data);
    }

    public static Response fail(String errorCode, String message) {
        return new Response(false, message, errorCode, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Object getData() {
        return data;
    }

    @Override
    public String toString() {
        return success ? "Response{OK, data=" + data + "}" : "Response{FAIL, " + errorCode + ": " + message + "}";
    }
}
