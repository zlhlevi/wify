package com.wify.common.resilience;

import com.wify.common.exception.LlmApiException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ResilienceProperties.class)
public class ResilienceConfig {

    @Bean
    public CircuitBreakerConfig providerCircuitBreakerConfig(ResilienceProperties resilienceProperties) {
        ResilienceProperties.CircuitBreakerProperties properties = resilienceProperties.getCircuitBreaker();
        return CircuitBreakerConfig.custom()
                .slidingWindowSize(properties.getSlidingWindowSize())
                .minimumNumberOfCalls(properties.getSlidingWindowSize())
                .failureRateThreshold(properties.getFailureRateThreshold())
                .waitDurationInOpenState(properties.getWaitDurationInOpenState())
                .permittedNumberOfCallsInHalfOpenState(properties.getPermittedNumberOfCallsInHalfOpenState())
                .recordException(this::shouldRecordFailure)
                .build();
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry(CircuitBreakerConfig providerCircuitBreakerConfig) {
        return CircuitBreakerRegistry.of(providerCircuitBreakerConfig);
    }

    @Bean(name = "timeoutRetryConfig")
    public RetryConfig timeoutRetryConfig(ResilienceProperties resilienceProperties) {
        ResilienceProperties.RetryProperties properties = resilienceProperties.getRetry();
        return RetryConfig.custom()
                .maxAttempts(properties.getTimeoutRetries() + 1)
                .waitDuration(properties.getTimeoutWaitDuration())
                .retryOnException(this::isTimeoutException)
                .build();
    }

    @Bean(name = "rateLimitRetryConfig")
    public RetryConfig rateLimitRetryConfig(ResilienceProperties resilienceProperties) {
        ResilienceProperties.RetryProperties properties = resilienceProperties.getRetry();
        Duration initialWaitDuration = properties.getRateLimitInitialWaitDuration();
        return RetryConfig.custom()
                .maxAttempts(properties.getRateLimitRetries() + 1)
                .intervalFunction(IntervalFunction.ofExponentialBackoff(
                        initialWaitDuration.toMillis(), properties.getRateLimitMultiplier()))
                .retryOnException(this::isRateLimitedException)
                .build();
    }

    @Bean
    public RetryRegistry retryRegistry() {
        return RetryRegistry.ofDefaults();
    }

    private boolean shouldRecordFailure(Throwable throwable) {
        LlmApiException llmApiException = unwrapLlmApiException(throwable);
        return llmApiException == null || llmApiException.getType() != LlmApiException.Type.AUTH_FAILED;
    }

    private boolean isTimeoutException(Throwable throwable) {
        LlmApiException llmApiException = unwrapLlmApiException(throwable);
        return llmApiException != null && llmApiException.getType() == LlmApiException.Type.TIMEOUT;
    }

    private boolean isRateLimitedException(Throwable throwable) {
        LlmApiException llmApiException = unwrapLlmApiException(throwable);
        return llmApiException != null && llmApiException.getType() == LlmApiException.Type.RATE_LIMITED;
    }

    private LlmApiException unwrapLlmApiException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof LlmApiException llmApiException) {
                return llmApiException;
            }
            current = current.getCause();
        }
        return null;
    }
}
