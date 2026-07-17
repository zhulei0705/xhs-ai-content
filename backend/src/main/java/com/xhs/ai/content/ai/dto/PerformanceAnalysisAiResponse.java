package com.xhs.ai.content.ai.dto;

import java.util.List;

public record PerformanceAnalysisAiResponse(
        String overallGrade,
        String performanceSummary,
        String topicAnalysis,
        String titleAnalysis,
        String favoriteRateAnalysis,
        String interactionRateAnalysis,
        List<String> nextTopicSuggestions) {
}
