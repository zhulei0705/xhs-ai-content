package com.xhs.ai.content.topic.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;

public record TopicGenerateRequest(
        @Min(value = 3, message = "选题数量不能少于3")
        @Max(value = 10, message = "选题数量不能超过10") Integer count,
        LocalDate generationDate) {

    public int actualCount() {
        return count == null ? 5 : count;
    }

    public LocalDate actualDate() {
        return generationDate == null ? LocalDate.now() : generationDate;
    }
}
