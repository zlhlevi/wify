package com.wify.provider.adapter;

import com.wify.provider.dto.ConnectionTestResult;
import com.wify.provider.entity.Provider;
import okhttp3.OkHttpClient;

import java.util.List;

public interface ProviderAdapter {

    /** 该 Adapter 支持的供应商类型（大写），可多个 */
    List<String> supportedTypes();

    /** 连通性测试 */
    ConnectionTestResult testConnection(Provider provider, OkHttpClient testClient);

    /** 拉取模型列表，返回模型 ID 列表 */
    List<String> listModels(Provider provider, OkHttpClient client);
}
