package com.xhs.ai.content.content.dto;

import jakarta.validation.constraints.NotNull;

public record ContentGenerateRequest(@NotNull(message = "选题ID不能为空") Long topicId) {
}
