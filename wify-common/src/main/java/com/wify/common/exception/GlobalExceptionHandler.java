package com.wify.common.exception;

import com.wify.common.constant.ErrorCode;
import com.wify.common.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return Result.fail(errorCode.getCode(), exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        ErrorCode errorCode = ErrorCode.INVALID_PARAM;
        return Result.fail(errorCode.getCode(), resolveValidationMessage(exception));
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception exception) {
        log.error("Unhandled exception", exception);
        ErrorCode errorCode = ErrorCode.SYSTEM_ERROR;
        return Result.fail(errorCode.getCode(), errorCode.getMessage());
    }

    private String resolveValidationMessage(MethodArgumentNotValidException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldError();
        if (fieldError != null && hasText(fieldError.getDefaultMessage())) {
            return fieldError.getDefaultMessage();
        }

        return exception.getBindingResult()
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .filter(this::hasText)
                .findFirst()
                .orElse(ErrorCode.INVALID_PARAM.getMessage());
    }

    private boolean hasText(String message) {
        return message != null && !message.isBlank();
    }
}
