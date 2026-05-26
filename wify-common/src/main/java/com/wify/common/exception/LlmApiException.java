package com.wify.common.exception;

public class LlmApiException extends RuntimeException {

    private final Type type;
    private final Integer statusCode;

    public LlmApiException(Type type, String message) {
        this(type, message, null, null);
    }

    public LlmApiException(Type type, String message, Throwable cause) {
        this(type, message, null, cause);
    }

    public LlmApiException(Type type, String message, Integer statusCode, Throwable cause) {
        super(message, cause);
        this.type = type;
        this.statusCode = statusCode;
    }

    public Type getType() {
        return type;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public enum Type {
        TIMEOUT,
        AUTH_FAILED,
        RATE_LIMITED,
        REQUEST_FAILED
    }
}
