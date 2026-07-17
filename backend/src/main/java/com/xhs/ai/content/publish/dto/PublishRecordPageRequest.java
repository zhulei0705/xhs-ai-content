package com.xhs.ai.content.publish.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PublishRecordPageRequest(@Min(1) Integer pageNum, @Min(1) @Max(100) Integer pageSize) {
    public long current() { return pageNum == null ? 1 : pageNum; }
    public long size() { return pageSize == null ? 10 : pageSize; }
}
