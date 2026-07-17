package com.xhs.ai.content.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xhs.ai.content.ai.dto.AiGenerationRequest;
import com.xhs.ai.content.ai.dto.AiProviderResponse;
import com.xhs.ai.content.common.exception.BizException;
import com.xhs.ai.content.common.exception.ErrorCode;
import com.xhs.ai.content.config.AiProperties;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiCompatibleProviderClient implements AiProviderClient {

    private static final Set<String> SUPPORTED = Set.of("openai", "deepseek", "openrouter", "compatible");

    private final AiProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(String provider) {
        return provider != null && SUPPORTED.contains(provider.toLowerCase(Locale.ROOT));
    }

    @Override
    public AiProviderResponse chat(AiGenerationRequest request) {
        validateConfiguration();
        RestClient client = createClient();
        Map<String, Object> payload = Map.of(
                "model", properties.getModel(),
                "temperature", properties.getTemperature(),
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "system", "content", request.systemPrompt()),
                        Map.of("role", "user", "content", request.userPrompt())));

        int attempts = Math.max(1, properties.getMaxAttempts());
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                String responseBody = client.post()
                        .uri(chatCompletionUrl())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(payload)
                        .retrieve()
                        .body(String.class);
                return parseResponse(responseBody);
            } catch (RestClientResponseException exception) {
                boolean retryable = exception.getStatusCode().value() == 429
                        || exception.getStatusCode().is5xxServerError();
                log.warn("AI provider returned status={}, attempt={}/{}",
                        exception.getStatusCode().value(), attempt, attempts);
                if (!retryable || attempt == attempts) {
                    throw new BizException(ErrorCode.AI_CALL_FAILED, "AI服务暂时不可用，请稍后重试");
                }
                backoff(attempt);
            } catch (BizException exception) {
                throw exception;
            } catch (Exception exception) {
                log.warn("AI provider request failed, attempt={}/{}: {}", attempt, attempts,
                        exception.getClass().getSimpleName());
                if (attempt == attempts) {
                    throw new BizException(ErrorCode.AI_CALL_FAILED, "AI服务连接失败，请检查配置或稍后重试");
                }
                backoff(attempt);
            }
        }
        throw new BizException(ErrorCode.AI_CALL_FAILED);
    }

    private RestClient createClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.getConnectTimeout())
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.getReadTimeout());
        return RestClient.builder().requestFactory(requestFactory).build();
    }

    private AiProviderResponse parseResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode contentNode = root.at("/choices/0/message/content");
        if (contentNode.isMissingNode() || !contentNode.isTextual()) {
            throw new BizException(ErrorCode.AI_RESPONSE_INVALID, "AI返回中缺少内容");
        }
        JsonNode usage = root.path("usage");
        return new AiProviderResponse(
                contentNode.asText(),
                nullableInt(usage.get("prompt_tokens")),
                nullableInt(usage.get("completion_tokens")));
    }

    private Integer nullableInt(JsonNode node) {
        return node == null || !node.canConvertToInt() ? null : node.asInt();
    }

    private String chatCompletionUrl() {
        String baseUrl = properties.getBaseUrl().replaceAll("/+$", "");
        return baseUrl.endsWith("/chat/completions") ? baseUrl : baseUrl + "/chat/completions";
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(properties.getApiKey())
                || !StringUtils.hasText(properties.getBaseUrl())
                || !StringUtils.hasText(properties.getModel())) {
            throw new BizException(ErrorCode.AI_CONFIGURATION_ERROR,
                    "请配置XHS_AI_BASE_URL、XHS_AI_API_KEY和XHS_AI_MODEL");
        }
    }

    private void backoff(int attempt) {
        try {
            Thread.sleep(300L * attempt);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BizException(ErrorCode.AI_CALL_FAILED, "AI请求已中断");
        }
    }
}
