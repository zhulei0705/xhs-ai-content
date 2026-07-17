package com.xhs.ai.content.ai.provider;

import com.xhs.ai.content.ai.dto.AiGenerationRequest;
import com.xhs.ai.content.ai.dto.AiProviderResponse;

/** AI Provider SPI；新增厂商时增加实现，不修改业务 Service。 */
public interface AiProviderClient {

    boolean supports(String provider);

    AiProviderResponse chat(AiGenerationRequest request);
}
