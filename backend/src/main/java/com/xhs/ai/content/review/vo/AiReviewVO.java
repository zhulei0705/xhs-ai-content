package com.xhs.ai.content.review.vo;

import java.time.LocalDateTime;
import java.util.List;

public record AiReviewVO(
        Long id,
        Long contentId,
        Integer aiToneScore,
        Integer titleAttractionScore,
        Integer authenticityScore,
        Integer platformFitScore,
        Boolean exaggeratedIncomeRisk,
        Boolean sensitiveExpressionRisk,
        Boolean marketingRisk,
        String riskLevel,
        String summary,
        List<String> suggestions,
        String provider,
        String modelName,
        LocalDateTime reviewedAt) {
}
