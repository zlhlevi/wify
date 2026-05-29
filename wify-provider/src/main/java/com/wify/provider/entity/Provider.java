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
@TableName(value = "provider", autoResultMap = true)
public class Provider extends BaseEntity {

    /** 供应商名称，唯一 */
    private String name;

    /** 供应商类型：OPENAI/ANTHROPIC/OLLAMA/OPENAI_COMPATIBLE */
    private String type;

    /** API基础地址 */
    private String baseUrl;

    /** 鉴权配置，结构按type不同 */
    @TableField(value = "auth_config", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> authConfig;

    /** 状态：0禁用 1启用 */
    private Integer enabled;
}
