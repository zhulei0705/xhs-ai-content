package com.xhs.ai.content.analysis.vo;

import java.math.BigDecimal;
import java.util.List;

public record DashboardVO(
        long publishedCount,
        long totalExposure,
        long totalLikes,
        long totalFavorites,
        long totalComments,
        long totalFollowerGrowth,
        BigDecimal averageFavoriteRate,
        BigDecimal averageInteractionRate,
        List<TopContent> topContents) {

    public record TopContent(Long contentId, String title, long exposureCount, BigDecimal interactionRate) {
    }
}
