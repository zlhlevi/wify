package com.wify.common.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.util.Objects;
import java.util.concurrent.Callable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class CircuitBreakerService {

    private static final String CIRCUIT_BREAKER_NAME_PREFIX = "provider-circuit-breaker-";
    private static final String TIMEOUT_RETRY_NAME_PREFIX = "provider-timeout-retry-";
    private static final String RATE_LIMIT_RETRY_NAME_PREFIX = "provider-rate-limit-retry-";

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final CircuitBreakerConfig providerCircuitBreakerConfig;
    private final RetryRegistry retryRegistry;
    private final RetryConfig timeoutRetryConfig;
    private final RetryConfig rateLimitRetryConfig;

    public CircuitBreakerService(
            CircuitBreakerRegistry circuitBreakerRegistry,
            CircuitBreakerConfig providerCircuitBreakerConfig,
            RetryRegistry retryRegistry,
            @Qualifier("timeoutRetryConfig") RetryConfig timeoutRetryConfig,
            @Qualifier("rateLimitRetryConfig") RetryConfig rateLimitRetryConfig) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.providerCircuitBreakerConfig = providerCircuitBreakerConfig;
        this.retryRegistry = retryRegistry;
        this.timeoutRetryConfig = timeoutRetryConfig;
        this.rateLimitRetryConfig = rateLimitRetryConfig;
    }

    public CircuitBreaker getCircuitBreaker(String providerName) {
        String normalizedProviderName = normalizeProviderName(providerName);
        String circuitBreakerName = CIRCUIT_BREAKER_NAME_PREFIX + normalizedProviderName;
        boolean exists = circuitBreakerRegistry.find(circuitBreakerName).isPresent();
        CircuitBreaker circuitBreaker =
                circuitBreakerRegistry.circuitBreaker(circuitBreakerName, providerCircuitBreakerConfig);
        if (!exists) {
            log.info("Created circuit breaker for providerName={}, circuitBreakerName={}",
                    normalizedProviderName, circuitBreakerName);
        }
        return circuitBreaker;
    }

    public <T> T execute(String providerName, Callable<T> callable) {
        Objects.requireNonNull(callable, "callable must not be null");

        Retry rateLimitRetry = getRateLimitRetry(providerName);
        Retry timeoutRetry = getTimeoutRetry(providerName);
        Callable<T> decoratedCallable = CircuitBreaker.decorateCallable(getCircuitBreaker(providerName), callable);
        decoratedCallable = Retry.decorateCallable(timeoutRetry, decoratedCallable);
        decoratedCallable = Retry.decorateCallable(rateLimitRetry, decoratedCallable);

        try {
            return decoratedCallable.call();
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to execute provider call with circuit breaker", exception);
        }
    }

    public void execute(String providerName, ThrowingRunnable runnable) {
        execute(providerName, () -> {
            runnable.run();
            return null;
        });
    }

    private Retry getTimeoutRetry(String providerName) {
        String normalizedProviderName = normalizeProviderName(providerName);
        String retryName = TIMEOUT_RETRY_NAME_PREFIX + normalizedProviderName;
        boolean exists = retryRegistry.find(retryName).isPresent();
        Retry retry = retryRegistry.retry(retryName, timeoutRetryConfig);
        if (!exists) {
            log.info("Created timeout retry for providerName={}, retryName={}",
                    normalizedProviderName, retryName);
        }
        return retry;
    }

    private Retry getRateLimitRetry(String providerName) {
        String normalizedProviderName = normalizeProviderName(providerName);
        String retryName = RATE_LIMIT_RETRY_NAME_PREFIX + normalizedProviderName;
        boolean exists = retryRegistry.find(retryName).isPresent();
        Retry retry = retryRegistry.retry(retryName, rateLimitRetryConfig);
        if (!exists) {
            log.info("Created rate limit retry for providerName={}, retryName={}",
                    normalizedProviderName, retryName);
        }
        return retry;
    }

    private String normalizeProviderName(String providerName) {
        return StringUtils.hasText(providerName) ? providerName.trim() : "default";
    }

    @FunctionalInterface
    public interface ThrowingRunnable {

        void run() throws Exception;
    }
}
