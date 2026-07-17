package com.xhs.ai.content.ai.dto;

import java.util.List;

public record ContentReviewAiResponse(
        Integer aiToneScore,
        Integer titleAttractionScore,
        Integer authenticityScore,
        Integer platformFitScore,
        Boolean exaggeratedIncomeRisk,
        Boolean sensitiveExpressionRisk,
        Boolean marketingRisk,
        String riskLevel,
        String summary,
        List<String> suggestions) {
}
