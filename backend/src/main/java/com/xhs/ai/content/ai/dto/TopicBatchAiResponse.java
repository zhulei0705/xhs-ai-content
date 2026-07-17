package com.xhs.ai.content.ai.dto;

import java.util.List;

public record TopicBatchAiResponse(List<TopicItem> topics) {

    public record TopicItem(
            String topicTitle,
            String coreAngle,
            String targetAudience,
            String contentType,
            List<String> keywords,
            Integer targetMatchScore,
            Integer viralPotentialScore,
            Integer overallScore,
            String potentialAnalysis,
            Integer publishPriority) {
    }
}
