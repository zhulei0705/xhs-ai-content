package com.xhs.ai.content.content.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ContentStatusTest {

    @Test
    void shouldEnforcePublishStateMachine() {
        assertThat(ContentStatus.DRAFT.canTransitionTo(ContentStatus.PENDING_REVIEW)).isTrue();
        assertThat(ContentStatus.PENDING_REVIEW.canTransitionTo(ContentStatus.READY_TO_PUBLISH)).isTrue();
        assertThat(ContentStatus.READY_TO_PUBLISH.canTransitionTo(ContentStatus.PUBLISHED)).isTrue();
        assertThat(ContentStatus.DRAFT.canTransitionTo(ContentStatus.PUBLISHED)).isFalse();
        assertThat(ContentStatus.PUBLISHED.canTransitionTo(ContentStatus.DRAFT)).isFalse();
        assertThat(ContentStatus.ARCHIVED.canTransitionTo(ContentStatus.DRAFT)).isFalse();
    }
}
