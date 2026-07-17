package com.xhs.ai.content.analysis.dto;

import jakarta.validation.constraints.NotNull;

public record AnalysisExecuteRequest(@NotNull Long publishRecordId) {
}
