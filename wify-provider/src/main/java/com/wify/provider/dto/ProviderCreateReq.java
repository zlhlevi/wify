package com.wify.provider.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Map;
import lombok.Data;

@Data
public class ProviderCreateReq {

    /** 供应商名称，唯一 */
    @NotBlank(message = "name 不能为空")
    @Size(max = 100, message = "name 长度不能超过 100")
    private String name;

    /** 供应商类型：OPENAI/ANTHROPIC/OLLAMA/OPENAI_COMPATIBLE */
    @NotBlank(message = "type 不能为空")
    @Pattern(
            regexp = "OPENAI|ANTHROPIC|OLLAMA|OPENAI_COMPATIBLE",
            message = "type 仅支持 OPENAI、ANTHROPIC、OLLAMA、OPENAI_COMPATIBLE")
    private String type;

    /** API基础地址 */
    @NotBlank(message = "baseUrl 不能为空")
    @Size(max = 500, message = "baseUrl 长度不能超过 500")
    private String baseUrl;

    /** 鉴权配置，结构按type不同 */
    private Map<String, Object> authConfig;

    /** 状态：0禁用 1启用 */
    private Integer enabled;
}
