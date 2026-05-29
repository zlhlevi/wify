package com.wify.provider.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wify.common.constant.ErrorCode;
import com.wify.common.dto.PageResult;
import com.wify.common.exception.BizException;
import com.wify.common.util.PageHelper;
import com.wify.provider.dto.ModelConfigResp;
import com.wify.provider.dto.ProviderCreateReq;
import com.wify.provider.dto.ProviderDetailResp;
import com.wify.provider.dto.ProviderHealthResp;
import com.wify.provider.dto.ProviderResp;
import com.wify.provider.dto.ProviderUpdateReq;
import com.wify.provider.entity.ModelConfig;
import com.wify.provider.entity.Provider;
import com.wify.provider.entity.ProviderHealth;
import com.wify.provider.mapper.ModelConfigMapper;
import com.wify.provider.mapper.ProviderHealthMapper;
import com.wify.provider.mapper.ProviderMapper;
import com.wify.provider.service.ProviderService;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ProviderServiceImpl implements ProviderService {

    private final ProviderMapper providerMapper;
    private final ModelConfigMapper modelConfigMapper;
    private final ProviderHealthMapper providerHealthMapper;

    public ProviderServiceImpl(
            ProviderMapper providerMapper,
            ModelConfigMapper modelConfigMapper,
            ProviderHealthMapper providerHealthMapper) {
        this.providerMapper = providerMapper;
        this.modelConfigMapper = modelConfigMapper;
        this.providerHealthMapper = providerHealthMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "provider-cache", allEntries = true)
    public Long create(ProviderCreateReq req) {
        validateNameUnique(req.getName(), null);

        Provider provider = new Provider();
        provider.setName(req.getName().trim());
        provider.setType(req.getType().trim());
        provider.setBaseUrl(req.getBaseUrl().trim());
        provider.setAuthConfig(req.getAuthConfig());
        provider.setEnabled(normalizeEnabled(req.getEnabled()));
        providerMapper.insert(provider);
        return provider.getId();
    }

    @Override
    @Cacheable(cacheNames = "provider-cache", key = "'detail:' + #id")
    public ProviderDetailResp getById(Long id) {
        Provider provider = getEntity(id);
        ProviderDetailResp resp = toDetailResp(provider);
        resp.setModelConfigs(getModelConfigRespList(provider.getId()));
        resp.setHealth(getProviderHealthResp(provider.getId()));
        return resp;
    }

    @Override
    @Cacheable(
            cacheNames = "provider-cache",
            key = "'list:' + (#page == null ? 1 : #page) + ':' + (#pageSize == null ? 20 : #pageSize)"
                    + " + ':' + (#type == null ? '' : #type) + ':' + (#enabled == null ? 'ALL' : #enabled)")
    public PageResult<ProviderResp> list(Integer page, Integer pageSize, String type, Integer enabled) {
        LambdaQueryWrapper<Provider> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.hasText(type), Provider::getType, type == null ? null : type.trim());
        queryWrapper.eq(enabled != null, Provider::getEnabled, enabled);
        queryWrapper.orderByDesc(Provider::getId);

        IPage<ProviderResp> result = providerMapper
                .selectPage(PageHelper.toPage(page, pageSize), queryWrapper)
                .convert(this::toResp);
        return PageHelper.toPageResult(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "provider-cache", allEntries = true)
    public Long update(Long id, ProviderUpdateReq req) {
        Provider provider = getEntity(id);
        validateNameUnique(req.getName(), id);

        provider.setName(req.getName().trim());
        provider.setType(req.getType().trim());
        provider.setBaseUrl(req.getBaseUrl().trim());
        provider.setAuthConfig(req.getAuthConfig());
        provider.setEnabled(normalizeEnabled(req.getEnabled()));
        providerMapper.updateById(provider);
        return provider.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "provider-cache", allEntries = true)
    public void delete(Long id) {
        Provider provider = getEntity(id);
        providerMapper.deleteById(provider.getId());

        LambdaUpdateWrapper<ModelConfig> modelConfigDeleteWrapper = new LambdaUpdateWrapper<>();
        modelConfigDeleteWrapper.eq(ModelConfig::getProviderId, provider.getId());
        modelConfigMapper.delete(modelConfigDeleteWrapper);

        LambdaQueryWrapper<ProviderHealth> providerHealthDeleteWrapper = new LambdaQueryWrapper<>();
        providerHealthDeleteWrapper.eq(ProviderHealth::getProviderId, provider.getId());
        providerHealthMapper.delete(providerHealthDeleteWrapper);
    }

    /**
     * 查询供应商实体，不存在时抛出业务异常。
     *
     * @param id 供应商ID
     * @return 供应商实体
     */
    private Provider getEntity(Long id) {
        Provider provider = providerMapper.selectById(id);
        if (provider == null) {
            throw new BizException(ErrorCode.PROVIDER_NOT_FOUND);
        }
        return provider;
    }

    /**
     * 校验供应商名称是否唯一。
     *
     * <p>更新场景会排除当前供应商自身，避免把自身名称误判为重复。</p>
     *
     * @param name 供应商名称
     * @param excludeId 需要排除的供应商ID，创建时为空
     */
    private void validateNameUnique(String name, Long excludeId) {
        String normalizedName = name == null ? null : name.trim();
        LambdaQueryWrapper<Provider> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Provider::getName, normalizedName);
        queryWrapper.ne(excludeId != null, Provider::getId, excludeId);
        Long count = providerMapper.selectCount(queryWrapper);
        if (count != null && count > 0) {
            throw new BizException(ErrorCode.PROVIDER_NAME_ALREADY_EXISTS);
        }
    }

    /**
     * 查询并转换指定供应商下的模型配置列表。
     *
     * @param providerId 供应商ID
     * @return 模型配置响应列表
     */
    private List<ModelConfigResp> getModelConfigRespList(Long providerId) {
        LambdaQueryWrapper<ModelConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ModelConfig::getProviderId, providerId);
        queryWrapper.orderByDesc(ModelConfig::getId);
        List<ModelConfig> modelConfigs = modelConfigMapper.selectList(queryWrapper);
        if (modelConfigs == null || modelConfigs.isEmpty()) {
            return Collections.emptyList();
        }
        return modelConfigs.stream()
                .map(this::toModelConfigResp)
                .collect(Collectors.toList());
    }

    /**
     * 查询并转换供应商健康状态信息。
     *
     * @param providerId 供应商ID
     * @return 健康状态响应对象，不存在时返回{@code null}
     */
    private ProviderHealthResp getProviderHealthResp(Long providerId) {
        LambdaQueryWrapper<ProviderHealth> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProviderHealth::getProviderId, providerId);
        ProviderHealth providerHealth = providerHealthMapper.selectOne(queryWrapper);
        if (providerHealth == null) {
            return null;
        }
        return toProviderHealthResp(providerHealth);
    }

    /**
     * 将供应商实体转换为列表响应对象。
     *
     * @param provider 供应商实体
     * @return 供应商列表响应
     */
    private ProviderResp toResp(Provider provider) {
        ProviderResp resp = new ProviderResp();
        copyProviderToResp(resp, provider);
        return resp;
    }

    /**
     * 将供应商实体转换为详情响应对象。
     *
     * @param provider 供应商实体
     * @return 供应商详情响应
     */
    private ProviderDetailResp toDetailResp(Provider provider) {
        ProviderDetailResp resp = new ProviderDetailResp();
        copyProviderToResp(resp, provider);
        return resp;
    }

    /**
     * 复制供应商基础字段到响应对象。
     *
     * @param resp 响应对象
     * @param provider 供应商实体
     * @param <T> 响应类型
     */
    private <T extends ProviderResp> void copyProviderToResp(T resp, Provider provider) {
        resp.setId(provider.getId());
        resp.setName(defaultString(provider.getName()));
        resp.setType(defaultString(provider.getType()));
        resp.setBaseUrl(defaultString(provider.getBaseUrl()));
        resp.setEnabled(provider.getEnabled());
        resp.setAuthConfigured(hasAuthConfig(provider.getAuthConfig()));
        resp.setCreatedAt(provider.getCreatedAt());
        resp.setUpdatedAt(provider.getUpdatedAt());
    }

    /**
     * 将模型配置实体转换为响应对象。
     *
     * @param modelConfig 模型配置实体
     * @return 模型配置响应
     */
    private ModelConfigResp toModelConfigResp(ModelConfig modelConfig) {
        ModelConfigResp resp = new ModelConfigResp();
        resp.setId(modelConfig.getId());
        resp.setProviderId(modelConfig.getProviderId());
        resp.setName(defaultString(modelConfig.getName()));
        resp.setModelId(defaultString(modelConfig.getModelId()));
        resp.setContextSize(modelConfig.getContextSize());
        resp.setExtraParams(modelConfig.getExtraParams());
        resp.setEnabled(modelConfig.getEnabled());
        resp.setCreatedAt(modelConfig.getCreatedAt());
        resp.setUpdatedAt(modelConfig.getUpdatedAt());
        return resp;
    }

    /**
     * 将供应商健康状态实体转换为响应对象。
     *
     * @param providerHealth 健康状态实体
     * @return 健康状态响应
     */
    private ProviderHealthResp toProviderHealthResp(ProviderHealth providerHealth) {
        ProviderHealthResp resp = new ProviderHealthResp();
        resp.setProviderId(providerHealth.getProviderId());
        resp.setStatus(defaultString(providerHealth.getStatus()));
        resp.setLastCheckAt(providerHealth.getLastCheckAt());
        resp.setLastSuccessAt(providerHealth.getLastSuccessAt());
        resp.setFailCount(providerHealth.getFailCount());
        resp.setLatencyMs(providerHealth.getLatencyMs());
        resp.setErrorMessage(defaultString(providerHealth.getErrorMessage()));
        resp.setUpdatedAt(providerHealth.getUpdatedAt());
        return resp;
    }

    /**
     * 规范化启用状态。
     *
     * <p>除显式传入0外，其他情况统一按启用处理。</p>
     *
     * @param enabled 请求中的启用状态
     * @return 规范化后的启用状态
     */
    private Integer normalizeEnabled(Integer enabled) {
        return Objects.equals(enabled, 0) ? 0 : 1;
    }

    /**
     * 判断鉴权配置是否已设置。
     *
     * @param authConfig 鉴权配置
     * @return 已配置返回true，否则返回false
     */
    private boolean hasAuthConfig(Map<String, Object> authConfig) {
        return authConfig != null && !authConfig.isEmpty();
    }

    /**
     * 将可能为空的字符串转换为空串，保证接口返回稳定。
     *
     * @param value 原始字符串
     * @return 非空字符串
     */
    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
