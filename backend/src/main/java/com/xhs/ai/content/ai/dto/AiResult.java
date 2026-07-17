package com.xhs.ai.content.ai.dto;

public record AiResult<T>(T data, String provider, String model, String taskNo) {
}
