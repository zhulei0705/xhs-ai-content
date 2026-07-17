package com.xhs.ai.content.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_PARAMETER(40000, "请求参数不正确"),
    DATA_NOT_FOUND(40400, "数据不存在"),
    CONFLICT(40900, "数据状态冲突"),
    AI_CONFIGURATION_ERROR(50010, "AI配置不完整"),
    AI_CALL_FAILED(50011, "AI服务调用失败"),
    AI_RESPONSE_INVALID(50012, "AI返回内容格式不正确"),
    SYSTEM_ERROR(50000, "系统繁忙，请稍后重试");

    private final int code;
    private final String message;
}
