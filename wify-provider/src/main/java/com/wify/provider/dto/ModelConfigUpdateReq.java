package com.wify.provider.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.Map;
import lombok.Data;

@Data
public class ModelConfigUpdateReq {

    /** 展示名，如GPT-4o */
    @NotBlank(message = "name 不能为空")
    @Size(max = 100, message = "name 长度不能超过 100")
    private String name;

    /** 调用时传给API的值 */
    @NotBlank(message = "modelId 不能为空")
    @Size(max = 100, message = "modelId 长度不能超过 100")
    private String modelId;

    /** 上下文窗口大小（token数） */
    @Positive(message = "contextSize 必须大于 0")
    private Integer contextSize;

    /** 模型级别扩展参数 */
    private Map<String, Object> extraParams;

    /** 状态：0禁用 1启用 */
    private Integer enabled;
}
