package com.wify.provider.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProviderDetailResp extends ProviderResp {

    /** 供应商下的模型配置列表 */
    private List<ModelConfigResp> modelConfigs = new ArrayList<>();
}
