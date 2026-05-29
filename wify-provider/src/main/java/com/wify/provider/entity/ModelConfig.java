package com.wify.provider.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.wify.common.entity.BaseEntity;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName(value = "model_config", autoResultMap = true)
public class ModelConfig extends BaseEntity {

    /** 提供商ID */
    private Long providerId;

    /** 展示名，如GPT-4o */
    private String name;

    /** 调用时传给API的值 */
    private String modelId;

    /** 上下文窗口大小（token数） */
    private Integer contextSize;

    /** 模型级别扩展参数 */
    @TableField(value = "extra_params", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extraParams;

    /** 状态：0禁用 1启用 */
    private Integer enabled;
}
