package com.xhs.ai.content.ai.service;

import com.xhs.ai.content.ai.dto.AiGenerationRequest;
import com.xhs.ai.content.ai.dto.AiResult;

/** 业务层唯一依赖的 AI 能力入口。 */
public interface AiModelService {

    <T> AiResult<T> generateStructured(AiGenerationRequest request, Class<T> responseType);
}
