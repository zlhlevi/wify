package com.wify.common.http;

import com.wify.common.exception.LlmApiException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Slf4j
@Component
public class LlmHttpClient {

    private static final MediaType DEFAULT_OKHTTP_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final org.springframework.http.MediaType DEFAULT_SPRING_MEDIA_TYPE =
            org.springframework.http.MediaType.APPLICATION_JSON;

    private final RestTemplate restTemplate;
    private final OkHttpClient okHttpClient;

    public LlmHttpClient(
            @Value("${wify.http.llm.connect-timeout-ms:5000}") int connectTimeoutMs,
            @Value("${wify.http.llm.read-timeout-ms:60000}") int readTimeoutMs,
            @Value("${wify.http.llm.stream-read-timeout-ms:120000}") int streamReadTimeoutMs) {
        this.restTemplate = createRestTemplate(connectTimeoutMs, readTimeoutMs);
        this.okHttpClient = createOkHttpClient(connectTimeoutMs, streamReadTimeoutMs);
    }

    public String post(String url, Map<String, String> headers, String body) {
        long startNanos = System.nanoTime();
        HttpHeaders httpHeaders = buildHttpHeaders(headers);
        HttpEntity<String> requestEntity = new HttpEntity<>(defaultBody(body), httpHeaders);

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            log.info("LLM POST success url={}, durationMs={}, statusCode={}",
                    url, elapsedMillis(startNanos), response.getStatusCode().value());
            return response.getBody();
        } catch (HttpStatusCodeException exception) {
            log.warn("LLM POST failed url={}, durationMs={}, statusCode={}",
                    url, elapsedMillis(startNanos), exception.getStatusCode().value());
            throw mapStatusException("LLM POST request failed", exception.getStatusCode(), exception);
        } catch (ResourceAccessException exception) {
            log.warn("LLM POST failed url={}, durationMs={}, statusCode=N/A",
                    url, elapsedMillis(startNanos));
            throw mapClientException("LLM POST request failed", exception);
        } catch (RestClientException exception) {
            log.error("LLM POST failed url={}, durationMs={}, statusCode=N/A",
                    url, elapsedMillis(startNanos), exception);
            throw new LlmApiException(
                    LlmApiException.Type.REQUEST_FAILED, "LLM POST request failed", exception);
        }
    }

    public void stream(String url, Map<String, String> headers, String body, Consumer<String> callback) {
        Objects.requireNonNull(callback, "callback must not be null");
        long startNanos = System.nanoTime();

        Request request = new Request.Builder()
                .url(url)
                .headers(buildOkHttpHeaders(headers))
                .post(RequestBody.create(resolveMediaType(headers), defaultBody(body)))
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            int statusCode = response.code();
            if (!response.isSuccessful()) {
                log.warn("LLM STREAM failed url={}, durationMs={}, statusCode={}",
                        url, elapsedMillis(startNanos), statusCode);
                throw mapStatusCode("LLM stream request failed", statusCode, null);
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                log.info("LLM STREAM completed url={}, durationMs={}, statusCode={}",
                        url, elapsedMillis(startNanos), statusCode);
                return;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(responseBody.byteStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    callback.accept(line);
                }
            }

            log.info("LLM STREAM completed url={}, durationMs={}, statusCode={}",
                    url, elapsedMillis(startNanos), statusCode);
        } catch (IOException exception) {
            log.warn("LLM STREAM failed url={}, durationMs={}, statusCode=N/A",
                    url, elapsedMillis(startNanos));
            throw mapClientException("LLM stream request failed", exception);
        }
    }

    private RestTemplate createRestTemplate(int connectTimeoutMs, int readTimeoutMs) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);
        return new RestTemplate(requestFactory);
    }

    private OkHttpClient createOkHttpClient(int connectTimeoutMs, int readTimeoutMs) {
        return new OkHttpClient.Builder()
                .connectTimeout(java.time.Duration.ofMillis(connectTimeoutMs))
                .readTimeout(java.time.Duration.ofMillis(readTimeoutMs))
                .build();
    }

    private HttpHeaders buildHttpHeaders(Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        safeHeaders(headers).forEach(httpHeaders::set);
        if (!httpHeaders.containsKey(HttpHeaders.CONTENT_TYPE)) {
            httpHeaders.setContentType(DEFAULT_SPRING_MEDIA_TYPE);
        }
        return httpHeaders;
    }

    private Headers buildOkHttpHeaders(Map<String, String> headers) {
        Headers.Builder builder = new Headers.Builder();
        safeHeaders(headers).forEach(builder::add);
        if (builder.get(HttpHeaders.CONTENT_TYPE) == null) {
            builder.add(HttpHeaders.CONTENT_TYPE, DEFAULT_OKHTTP_MEDIA_TYPE.toString());
        }
        return builder.build();
    }

    private MediaType resolveMediaType(Map<String, String> headers) {
        String contentType = safeHeaders(headers).get(HttpHeaders.CONTENT_TYPE);
        if (contentType == null || contentType.isBlank()) {
            return DEFAULT_OKHTTP_MEDIA_TYPE;
        }
        MediaType mediaType = MediaType.parse(contentType);
        return mediaType == null ? DEFAULT_OKHTTP_MEDIA_TYPE : mediaType;
    }

    private Map<String, String> safeHeaders(Map<String, String> headers) {
        return headers == null ? Collections.emptyMap() : headers;
    }

    private String defaultBody(String body) {
        return body == null ? "" : body;
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }

    private LlmApiException mapStatusException(
            String message, HttpStatusCode statusCode, Exception exception) {
        return mapStatusCode(message, statusCode.value(), exception);
    }

    private LlmApiException mapStatusCode(String message, int statusCode, Exception exception) {
        if (statusCode == 401 || statusCode == 403) {
            return new LlmApiException(LlmApiException.Type.AUTH_FAILED, message, statusCode, exception);
        }
        if (statusCode == 429) {
            return new LlmApiException(LlmApiException.Type.RATE_LIMITED, message, statusCode, exception);
        }
        if (statusCode == 408) {
            return new LlmApiException(LlmApiException.Type.TIMEOUT, message, statusCode, exception);
        }
        return new LlmApiException(LlmApiException.Type.REQUEST_FAILED, message, statusCode, exception);
    }

    private LlmApiException mapClientException(String message, Exception exception) {
        if (isTimeoutException(exception)) {
            return new LlmApiException(LlmApiException.Type.TIMEOUT, message, exception);
        }
        return new LlmApiException(LlmApiException.Type.REQUEST_FAILED, message, exception);
    }

    private boolean isTimeoutException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SocketTimeoutException || current instanceof InterruptedIOException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
