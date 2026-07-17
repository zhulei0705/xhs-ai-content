package com.xhs.ai.content.ai.dto;

public record AiProviderResponse(String content, Integer promptTokens, Integer completionTokens) {
}
