package com.xhs.ai.content.topic.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record TopicPageRequest(
        @Min(value = 1, message = "页码不能小于1") Integer pageNum,
        @Min(value = 1, message = "每页数量不能小于1")
        @Max(value = 100, message = "每页数量不能超过100") Integer pageSize,
        String status,
        LocalDate generationDate,
        @Size(max = 100, message = "关键词不能超过100字") String keyword) {

    public long current() { return pageNum == null ? 1 : pageNum; }
    public long size() { return pageSize == null ? 10 : pageSize; }
}
