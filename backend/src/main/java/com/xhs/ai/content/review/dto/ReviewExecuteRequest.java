package com.xhs.ai.content.review.dto;

import jakarta.validation.constraints.NotNull;

public record ReviewExecuteRequest(@NotNull Long contentId) {
}
