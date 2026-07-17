package com.xhs.ai.content.content.vo;

import com.xhs.ai.content.review.vo.AiReviewVO;
import java.time.LocalDateTime;
import java.util.List;

public record ContentDetailVO(
        Long id,
        Long topicId,
        String selectedTitle,
        String body,
        String coverTitle,
        String coverSubtitle,
        List<String> tags,
        String interactionGuide,
        String status,
        List<TitleVO> titles,
        List<CardVO> cards,
        AiReviewVO latestReview,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public record TitleVO(Long id, String text, Integer sortOrder, Boolean selected, Integer attractionScore) {
    }

    public record CardVO(Long id, Integer cardNo, String title, String body) {
    }
}
