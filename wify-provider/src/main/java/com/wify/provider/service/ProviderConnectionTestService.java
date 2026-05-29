package com.wify.provider.service;

import com.wify.common.constant.ErrorCode;
import com.wify.common.exception.BizException;
import com.wify.provider.adapter.ProviderAdapterFactory;
import com.wify.provider.dto.ConnectionTestResult;
import com.wify.provider.entity.Provider;
import com.wify.provider.mapper.ProviderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import okhttp3.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProviderConnectionTestService {

    private final ProviderAdapterFactory adapterFactory;
    private final ProviderMapper providerMapper;

    /** 连通性测试专用 10s 超时客户端 */
    private final OkHttpClient testClient = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    /**
     * 根据供应商ID执行连通性测试。
     *
     * @param id 供应商ID
     * @return 连通性测试结果
     */
    public ConnectionTestResult testConnection(Long id) {
        return testById(id);
    }

    /**
     * 根据供应商ID执行连通性测试。
     *
     * @param id 供应商ID
     * @return 连通性测试结果
     */
    public ConnectionTestResult testById(Long id) {
        Provider provider = providerMapper.selectById(id);
        if (provider == null) {
            throw new BizException(ErrorCode.PROVIDER_NOT_FOUND);
        }
        return test(provider);
    }

    public ConnectionTestResult test(Provider provider) {
        log.info("连通性测试 provider={} type={}", provider.getName(), provider.getType());
        return adapterFactory.get(provider.getType()).testConnection(provider, testClient);
    }
}
