package com.wify.common.constant;

public enum ErrorCode {
    // 1000-1999 通用
    INVALID_PARAM(1000, "invalid parameter"),
    UNAUTHORIZED(1001, "unauthorized"),
    FORBIDDEN(1002, "forbidden"),
    NOT_FOUND(1003, "resource not found"),
    METHOD_NOT_ALLOWED(1004, "method not allowed"),
    REQUEST_TIMEOUT(1005, "request timeout"),
    TOO_MANY_REQUESTS(1006, "too many requests"),
    SERVICE_UNAVAILABLE(1007, "service unavailable"),
    SYSTEM_ERROR(1999, "internal system error"),

    // 2000-2999 Provider
    PROVIDER_NOT_FOUND(2000, "provider not found"),
    PROVIDER_NAME_ALREADY_EXISTS(2001, "provider name already exists"),
    PROVIDER_TYPE_NOT_SUPPORTED(2002, "provider type not supported");

    // 3000-3999 Agent

    // 4000-4999 Chat

    // 5000-5999 MCP

    // 6000-6999 Workflow

    // 7000-7999 Knowledge

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
