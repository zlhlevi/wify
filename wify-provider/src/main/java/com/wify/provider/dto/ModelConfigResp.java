package com.wify.provider.dto;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;

@Data
public class ModelConfigResp {

    /** 主键ID */
    private Long id;

    /** 提供商ID */
    private Long providerId;

    /** 展示名，如GPT-4o */
    private String name = "";

    /** 调用时传给API的值 */
    private String modelId = "";

    /** 上下文窗口大小（token数） */
    private Integer contextSize;

    /** 模型级别扩展参数 */
    private Map<String, Object> extraParams;

    /** 状态：0禁用 1启用 */
    private Integer enabled;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
