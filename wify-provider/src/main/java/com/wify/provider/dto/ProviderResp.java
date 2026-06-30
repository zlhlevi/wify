package com.wify.provider.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ProviderResp {

    /** 主键ID */
    private Long id;

    /** 供应商名称，唯一 */
    private String name = "";

    /** 供应商类型：OPENAI/ANTHROPIC/OLLAMA/OPENAI_COMPATIBLE */
    private String type = "";

    /** API基础地址 */
    private String baseUrl = "";

    /** 状态：0禁用 1启用 */
    private Integer enabled;

    /** 是否已配置鉴权信息 */
    private Boolean authConfigured = Boolean.FALSE;

    /** 供应商下已启用的模型数量 */
    private Integer enabledModelCount = 0;

    /** 供应商健康状态 */
    private ProviderHealthResp health;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
