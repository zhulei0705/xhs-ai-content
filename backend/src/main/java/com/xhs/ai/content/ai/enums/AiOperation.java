package com.xhs.ai.content.ai.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AiOperation {
    TOPIC_GENERATION("topic-v1"),
    CONTENT_GENERATION("content-v1"),
    CONTENT_REVIEW("review-v1"),
    PERFORMANCE_ANALYSIS("analysis-v1");

    private final String promptVersion;
}
