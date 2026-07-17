package com.xhs.ai.content.common.dto;

import jakarta.validation.constraints.NotNull;

public record IdRequest(@NotNull(message = "ID不能为空") Long id) {
}
