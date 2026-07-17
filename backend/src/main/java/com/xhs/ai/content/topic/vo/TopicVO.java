package com.xhs.ai.content.topic.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record TopicVO(
        Long id,
        String topicTitle,
        String coreAngle,
        String targetAudience,
        String contentType,
        List<String> keywords,
        Integer targetMatchScore,
        Integer viralPotentialScore,
        Integer overallScore,
        String potentialAnalysis,
        Integer publishPriority,
        String sourceType,
        LocalDate generationDate,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
