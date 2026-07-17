package com.xhs.ai.content.content.dto;

import com.xhs.ai.content.content.enums.ContentStatus;
import jakarta.validation.constraints.NotNull;

public record ContentStatusUpdateRequest(@NotNull Long id, @NotNull ContentStatus targetStatus) {
}
