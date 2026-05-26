package com.wify.common.log;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class RequestLogInterceptor implements HandlerInterceptor {

    private static final String TRACE_ID_KEY = "traceId";
    private static final String TRACE_ID_ATTRIBUTE = RequestLogInterceptor.class.getName() + ".TRACE_ID";
    private static final String START_TIME_ATTRIBUTE = RequestLogInterceptor.class.getName() + ".START_TIME";
    private static final long SLOW_REQUEST_THRESHOLD_MS = 1000L;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String traceId = UUID.randomUUID().toString().replace("-", "");
        request.setAttribute(TRACE_ID_ATTRIBUTE, traceId);
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        MDC.put(TRACE_ID_KEY, traceId);
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            @Nullable Exception exception) {
        try {
            Object startTime = request.getAttribute(START_TIME_ATTRIBUTE);
            long durationMs = 0L;
            if (startTime instanceof Long startTimeMillis) {
                durationMs = System.currentTimeMillis() - startTimeMillis;
            }

            String method = request.getMethod();
            String path = request.getRequestURI();
            int status = response.getStatus();

            if (durationMs > SLOW_REQUEST_THRESHOLD_MS) {
                log.warn("request method={}, path={}, status={}, durationMs={}", method, path, status, durationMs);
            } else {
                log.info("request method={}, path={}, status={}, durationMs={}", method, path, status, durationMs);
            }
        } finally {
            MDC.remove(TRACE_ID_KEY);
        }
    }
}
