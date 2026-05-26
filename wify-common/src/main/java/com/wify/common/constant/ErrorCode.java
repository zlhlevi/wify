package com.wify.common.constant;

public enum ErrorCode {

    INVALID_PARAM(1000, "invalid parameter"),
    UNAUTHORIZED(1001, "unauthorized"),
    FORBIDDEN(1002, "forbidden"),
    NOT_FOUND(1003, "resource not found"),
    METHOD_NOT_ALLOWED(1004, "method not allowed"),
    REQUEST_TIMEOUT(1005, "request timeout"),
    TOO_MANY_REQUESTS(1006, "too many requests"),
    SERVICE_UNAVAILABLE(1007, "service unavailable"),
    SYSTEM_ERROR(1999, "internal system error");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
