package com.wify.common.resilience;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wify.resilience")
public class ResilienceProperties {

    /**
     * 熔断器配置。
     */
    private final CircuitBreakerProperties circuitBreaker = new CircuitBreakerProperties();

    /**
     * 重试配置。
     */
    private final RetryProperties retry = new RetryProperties();

    public CircuitBreakerProperties getCircuitBreaker() {
        return circuitBreaker;
    }

    public RetryProperties getRetry() {
        return retry;
    }

    public static class CircuitBreakerProperties {

        /**
         * 滑动窗口大小，用于统计最近调用结果。
         */
        private int slidingWindowSize = 10;

        /**
         * 触发熔断的失败率阈值，单位百分比。
         */
        private float failureRateThreshold = 50F;

        /**
         * 熔断器打开后保持打开状态的时长。
         */
        private Duration waitDurationInOpenState = Duration.ofSeconds(30);

        /**
         * 半开状态下允许通过的调用次数。
         */
        private int permittedNumberOfCallsInHalfOpenState = 3;

        public int getSlidingWindowSize() {
            return slidingWindowSize;
        }

        public void setSlidingWindowSize(int slidingWindowSize) {
            this.slidingWindowSize = slidingWindowSize;
        }

        public float getFailureRateThreshold() {
            return failureRateThreshold;
        }

        public void setFailureRateThreshold(float failureRateThreshold) {
            this.failureRateThreshold = failureRateThreshold;
        }

        public Duration getWaitDurationInOpenState() {
            return waitDurationInOpenState;
        }

        public void setWaitDurationInOpenState(Duration waitDurationInOpenState) {
            this.waitDurationInOpenState = waitDurationInOpenState;
        }

        public int getPermittedNumberOfCallsInHalfOpenState() {
            return permittedNumberOfCallsInHalfOpenState;
        }

        public void setPermittedNumberOfCallsInHalfOpenState(int permittedNumberOfCallsInHalfOpenState) {
            this.permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState;
        }
    }

    public static class RetryProperties {

        /**
         * 网络超时场景的重试次数。
         */
        private int timeoutRetries = 2;

        /**
         * 网络超时场景的固定重试间隔。
         */
        private Duration timeoutWaitDuration = Duration.ofSeconds(1);

        /**
         * 限流场景的重试次数。
         */
        private int rateLimitRetries = 2;

        /**
         * 限流场景第一次重试的等待时长。
         */
        private Duration rateLimitInitialWaitDuration = Duration.ofSeconds(2);

        /**
         * 限流场景指数退避的倍率。
         */
        private double rateLimitMultiplier = 2.0D;

        public int getTimeoutRetries() {
            return timeoutRetries;
        }

        public void setTimeoutRetries(int timeoutRetries) {
            this.timeoutRetries = timeoutRetries;
        }

        public Duration getTimeoutWaitDuration() {
            return timeoutWaitDuration;
        }

        public void setTimeoutWaitDuration(Duration timeoutWaitDuration) {
            this.timeoutWaitDuration = timeoutWaitDuration;
        }

        public int getRateLimitRetries() {
            return rateLimitRetries;
        }

        public void setRateLimitRetries(int rateLimitRetries) {
            this.rateLimitRetries = rateLimitRetries;
        }

        public Duration getRateLimitInitialWaitDuration() {
            return rateLimitInitialWaitDuration;
        }

        public void setRateLimitInitialWaitDuration(Duration rateLimitInitialWaitDuration) {
            this.rateLimitInitialWaitDuration = rateLimitInitialWaitDuration;
        }

        public double getRateLimitMultiplier() {
            return rateLimitMultiplier;
        }

        public void setRateLimitMultiplier(double rateLimitMultiplier) {
            this.rateLimitMultiplier = rateLimitMultiplier;
        }
    }
}
