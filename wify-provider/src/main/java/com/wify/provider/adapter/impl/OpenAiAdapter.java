package com.wify.provider.adapter.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wify.common.exception.LlmApiException;
import com.wify.common.http.LlmHttpClient;
import com.wify.provider.adapter.ProviderAdapter;
import com.wify.provider.dto.ConnectionTestResult;
import com.wify.provider.entity.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Profile("!mock")
@RequiredArgsConstructor
public class OpenAiAdapter implements ProviderAdapter {

    protected final LlmHttpClient llmHttpClient;
    protected final ObjectMapper objectMapper;

    @Override
    public List<String> supportedTypes() {
        return List.of("OPENAI");
    }

    // ── 连通性测试 ─────────────────────────────────────────────

    @Override
    public ConnectionTestResult testConnection(Provider provider, OkHttpClient testClient) {
        long start = System.currentTimeMillis();
        try {
            String body = llmHttpClient.get(modelsUrl(provider), authHeaders(provider), testClient);
            int latency = (int) (System.currentTimeMillis() - start);
            return ConnectionTestResult.ok(latency, parseDataArraySize(body));
        } catch (LlmApiException e) {
            return ConnectionTestResult.fail(e.getMessage());
        } catch (Exception e) {
            return ConnectionTestResult.fail("测试异常：" + e.getMessage());
        }
    }

    @Override
    public List<String> listModels(Provider provider, OkHttpClient client) {
        try {
            String body = llmHttpClient.get(modelsUrl(provider), authHeaders(provider), client);
            return parseDataIds(body);
        } catch (Exception e) {
            log.warn("listModels failed provider={}: {}", provider.getName(), e.getMessage());
            return List.of();
        }
    }

    // ── 供子类复用的工具方法 ────────────────────────────────────

    protected String chatUrl(Provider provider) {
        return provider.getBaseUrl().stripTrailing() + "/v1/chat/completions";
    }

    protected String modelsUrl(Provider provider) {
        return provider.getBaseUrl().stripTrailing() + "/v1/models";
    }

    protected Map<String, String> authHeaders(Provider provider) {
        return Map.of("Authorization", "Bearer " + getAuth(provider, "apiKey"));
    }

    protected String parseDelta(String data) {
        try {
            JsonNode root = objectMapper.readTree(data);
            return root.path("choices").path(0).path("delta").path("content").asText(null);
        } catch (Exception e) {
            return null;
        }
    }

    protected String parseFinishReason(String data) {
        try {
            JsonNode reason = objectMapper.readTree(data)
                    .path("choices").path(0).path("finish_reason");
            return reason.isNull() || reason.isMissingNode() ? null : reason.asText();
        } catch (Exception e) {
            return null;
        }
    }

    protected String getAuth(Provider provider, String key) {
        Map<String, Object> auth = provider.getAuthConfig();
        if (auth == null || !auth.containsKey(key) || auth.get(key) == null) {
            throw new IllegalArgumentException("authConfig 缺少字段：" + key);
        }
        return auth.get(key).toString();
    }

    protected int parseDataArraySize(String body) {
        try {
            JsonNode data = objectMapper.readTree(body).path("data");
            return data.isArray() ? data.size() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    protected List<String> parseDataIds(String body) {
        List<String> ids = new ArrayList<>();
        try {
            JsonNode data = objectMapper.readTree(body).path("data");
            if (data.isArray()) {
                data.forEach(n -> {
                    JsonNode id = n.path("id");
                    if (!id.isMissingNode()) {
                        ids.add(id.asText());
                    }
                });
            }
        } catch (Exception e) {
            log.warn("parseDataIds failed: {}", e.getMessage());
        }
        return ids;
    }
}
