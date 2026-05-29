package com.wify.provider.service;

import com.wify.common.dto.PageResult;
import com.wify.provider.dto.ProviderCreateReq;
import com.wify.provider.dto.ProviderDetailResp;
import com.wify.provider.dto.ProviderResp;
import com.wify.provider.dto.ProviderUpdateReq;

public interface ProviderService {

    /**
     * 创建模型提供商。
     *
     * <p>创建前会校验供应商名称是否重复，并持久化基础配置与鉴权配置。</p>
     *
     * @param req 创建请求
     * @return 新创建的供应商ID
     */
    Long create(ProviderCreateReq req);

    /**
     * 根据ID查询模型提供商详情。
     *
     * <p>返回结果除了供应商基础信息外，还会包含关联的模型配置列表和健康状态信息。</p>
     *
     * @param id 供应商ID
     * @return 供应商详情
     */
    ProviderDetailResp getById(Long id);

    /**
     * 分页查询模型提供商列表。
     *
     * <p>支持按供应商类型和启用状态筛选，并按ID倒序返回。</p>
     *
     * @param page 页码，从1开始
     * @param pageSize 每页大小
     * @param type 供应商类型，可为空
     * @param enabled 启用状态，可为空
     * @return 分页后的供应商列表
     */
    PageResult<ProviderResp> list(Integer page, Integer pageSize, String type, Integer enabled);

    /**
     * 更新模型提供商。
     *
     * <p>更新前会校验供应商是否存在，以及更新后的名称是否与其他供应商重复。</p>
     *
     * @param id 供应商ID
     * @param req 更新请求
     * @return 更新后的供应商ID
     */
    Long update(Long id, ProviderUpdateReq req);

    /**
     * 删除模型提供商。
     *
     * <p>删除时会同时清理关联的模型配置和健康状态记录。</p>
     *
     * @param id 供应商ID
     */
    void delete(Long id);
}
