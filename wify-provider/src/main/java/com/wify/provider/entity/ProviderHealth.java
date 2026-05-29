package com.wify.provider.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("provider_health")
public class ProviderHealth implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 供应商ID，唯一索引 */
    private Long providerId;

    /** 健康状态：UP/DOWN/DEGRADED/UNKNOWN */
    private String status;

    /** 最后探测时间 */
    private LocalDateTime lastCheckAt;

    /** 最后成功时间 */
    private LocalDateTime lastSuccessAt;

    /** 连续失败次数 */
    private Integer failCount;

    /** 最近一次延迟，单位毫秒 */
    private Integer latencyMs;

    /** 最近失败原因 */
    private String errorMessage;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
