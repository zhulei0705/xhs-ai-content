package com.xhs.ai.content.common.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PageRequest(
        @Min(value = 1, message = "页码不能小于1") Integer pageNum,
        @Min(value = 1, message = "每页数量不能小于1")
        @Max(value = 100, message = "每页数量不能超过100") Integer pageSize) {

    public long current() {
        return pageNum == null ? 1 : pageNum;
    }

    public long size() {
        return pageSize == null ? 10 : pageSize;
    }
}
