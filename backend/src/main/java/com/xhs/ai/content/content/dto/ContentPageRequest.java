package com.xhs.ai.content.content.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ContentPageRequest(
        @Min(1) Integer pageNum,
        @Min(1) @Max(100) Integer pageSize,
        String status,
        @Size(max = 100) String keyword) {

    public long current() { return pageNum == null ? 1 : pageNum; }
    public long size() { return pageSize == null ? 10 : pageSize; }
}
