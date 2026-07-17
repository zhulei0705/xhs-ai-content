package com.xhs.ai.content.content.enums;

import java.util.EnumSet;
import java.util.Map;

public enum ContentStatus {
    DRAFT,
    PENDING_REVIEW,
    READY_TO_PUBLISH,
    PUBLISHED,
    ARCHIVED;

    private static final Map<ContentStatus, EnumSet<ContentStatus>> TRANSITIONS = Map.of(
            DRAFT, EnumSet.of(PENDING_REVIEW, ARCHIVED),
            PENDING_REVIEW, EnumSet.of(DRAFT, READY_TO_PUBLISH, ARCHIVED),
            READY_TO_PUBLISH, EnumSet.of(DRAFT, PENDING_REVIEW, PUBLISHED, ARCHIVED),
            PUBLISHED, EnumSet.of(ARCHIVED),
            ARCHIVED, EnumSet.noneOf(ContentStatus.class));

    public boolean canTransitionTo(ContentStatus target) {
        return this == target || TRANSITIONS.get(this).contains(target);
    }
}
