package com.xhs.ai.content.analysis.vo;

import java.time.LocalDateTime;
import java.util.List;

public record PerformanceAnalysisVO(
        Long id,
        Long publishRecordId,
        String overallGrade,
        String performanceSummary,
        String topicAnalysis,
        String titleAnalysis,
        String favoriteRateAnalysis,
        String interactionRateAnalysis,
        List<String> nextTopicSuggestions,
        String provider,
        String modelName,
        LocalDateTime analyzedAt) {
}
