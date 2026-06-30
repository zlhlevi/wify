package com.wify.provider.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProviderDetailResp extends ProviderResp {

    /** 鉴权配置，结构按type不同 */
    private Map<String, Object> authConfig;

    /** 供应商下的模型配置列表 */
    private List<ModelConfigResp> modelConfigs = new ArrayList<>();
}
