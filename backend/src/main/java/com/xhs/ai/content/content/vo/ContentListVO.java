package com.xhs.ai.content.content.vo;

import java.time.LocalDateTime;
import java.util.List;

public record ContentListVO(
        Long id,
        Long topicId,
        String selectedTitle,
        String coverTitle,
        List<String> tags,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
