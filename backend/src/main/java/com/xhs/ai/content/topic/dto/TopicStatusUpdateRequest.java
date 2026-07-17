package com.xhs.ai.content.topic.dto;

import com.xhs.ai.content.topic.enums.TopicStatus;
import jakarta.validation.constraints.NotNull;

public record TopicStatusUpdateRequest(@NotNull Long id, @NotNull TopicStatus status) {
}
