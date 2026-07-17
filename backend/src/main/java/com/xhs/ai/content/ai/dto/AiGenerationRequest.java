package com.xhs.ai.content.ai.dto;

import com.xhs.ai.content.ai.enums.AiOperation;

public record AiGenerationRequest(
        AiOperation operation,
        String promptVersion,
        Long bizId,
        String systemPrompt,
        String userPrompt) {
}
