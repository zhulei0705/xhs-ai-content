package com.xhs.ai.content.review.dto;

import jakarta.validation.constraints.NotNull;

public record ReviewConfirmRequest(@NotNull Long contentId, @NotNull Boolean approved) {
}
