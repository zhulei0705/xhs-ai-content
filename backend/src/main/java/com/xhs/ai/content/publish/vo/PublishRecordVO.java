package com.xhs.ai.content.publish.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PublishRecordVO(
        Long id,
        Long contentId,
        String contentTitle,
        LocalDateTime publishedAt,
        String noteUrl,
        long exposureCount,
        long likeCount,
        long favoriteCount,
        long commentCount,
        Integer followerGrowth,
        BigDecimal favoriteRate,
        BigDecimal interactionRate,
        LocalDateTime updatedAt) {
}
