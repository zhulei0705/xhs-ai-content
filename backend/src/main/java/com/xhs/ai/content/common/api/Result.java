package com.xhs.ai.content.common.api;

import java.time.Instant;

/** 统一 Web 响应。code=0 表示成功。 */
public record Result<T>(int code, String message, T data, long timestamp) {

    public static <T> Result<T> success(T data) {
        return new Result<>(0, "success", data, Instant.now().toEpochMilli());
    }

    public static Result<Void> success() {
        return success(null);
    }

    public static Result<Void> failure(int code, String message) {
        return new Result<>(code, message, null, Instant.now().toEpochMilli());
    }
}
