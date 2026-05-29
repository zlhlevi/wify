package com.wify.provider.dto;

import lombok.Data;

@Data
public class ConnectionTestResult {

    /** 连通性测试是否成功 */
    private Boolean success = Boolean.FALSE;

    /** 本次测试延迟，单位毫秒 */
    private Integer latencyMs = 0;

    /** 接口返回的模型数量 */
    private Integer modelCount = 0;

    /** 本次测试失败原因 */
    private String errorMessage = "";

    public static ConnectionTestResult ok(int latencyMs, int modelCount) {
        ConnectionTestResult r = new ConnectionTestResult();
        r.success = true;
        r.latencyMs = latencyMs;
        r.modelCount = modelCount;
        return r;
    }

    public static ConnectionTestResult fail(String errorMessage) {
        ConnectionTestResult r = new ConnectionTestResult();
        r.success = false;
        r.errorMessage = errorMessage;
        return r;
    }
}
