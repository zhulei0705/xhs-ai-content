package com.xhs.ai.content.ai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xhs.ai.content.ai.dto.AiGenerationRequest;
import com.xhs.ai.content.ai.dto.AiProviderResponse;
import com.xhs.ai.content.ai.dto.AiResult;
import com.xhs.ai.content.ai.entity.AiTaskEntity;
import com.xhs.ai.content.ai.mapper.AiTaskMapper;
import com.xhs.ai.content.ai.provider.AiProviderClient;
import com.xhs.ai.content.ai.service.AiModelService;
import com.xhs.ai.content.common.exception.BizException;
import com.xhs.ai.content.common.exception.ErrorCode;
import com.xhs.ai.content.config.AiProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultAiModelService implements AiModelService {

    private final List<AiProviderClient> providerClients;
    private final AiProperties properties;
    private final AiTaskMapper aiTaskMapper;
    private final ObjectMapper objectMapper;

    @Override
    public <T> AiResult<T> generateStructured(AiGenerationRequest request, Class<T> responseType) {
        AiProviderClient provider = providerClients.stream()
                .filter(client -> client.supports(properties.getProvider()))
                .findFirst()
                .orElseThrow(() -> new BizException(ErrorCode.AI_CONFIGURATION_ERROR,
                        "不支持的AI Provider: " + properties.getProvider()));

        AiTaskEntity task = createTask(request);
        aiTaskMapper.insert(task);
        long startNanos = System.nanoTime();
        try {
            AiProviderResponse providerResponse = provider.chat(request);
            String json = stripMarkdownFence(providerResponse.content());
            T result = objectMapper.readValue(json, responseType);
            completeTask(task, json, providerResponse, startNanos);
            return new AiResult<>(result, properties.getProvider(), properties.getModel(), task.getTaskNo());
        } catch (BizException exception) {
            failTask(task, exception.getMessage(), startNanos);
            throw exception;
        } catch (JsonProcessingException exception) {
            failTask(task, "AI返回JSON无法解析", startNanos);
            log.warn("AI response JSON parse failed, taskNo={}", task.getTaskNo());
            throw new BizException(ErrorCode.AI_RESPONSE_INVALID, "AI返回格式不正确，请重新生成");
        } catch (Exception exception) {
            failTask(task, "AI任务执行失败: " + exception.getClass().getSimpleName(), startNanos);
            throw new BizException(ErrorCode.AI_CALL_FAILED);
        }
    }

    private AiTaskEntity createTask(AiGenerationRequest request) {
        AiTaskEntity task = new AiTaskEntity();
        task.setTaskNo("AI" + UUID.randomUUID().toString().replace("-", ""));
        task.setTaskType(request.operation().name());
        task.setBizId(request.bizId());
        task.setStatus("RUNNING");
        task.setProvider(properties.getProvider());
        task.setModelName(properties.getModel());
        task.setPromptVersion(request.promptVersion());
        task.setRequestHash(sha256(request.systemPrompt() + "\n" + request.userPrompt()));
        task.setStartedAt(LocalDateTime.now());
        return task;
    }

    private void completeTask(AiTaskEntity task, String json, AiProviderResponse response, long startNanos) {
        task.setStatus("SUCCESS");
        task.setResultSummary(truncate(json, 1000));
        task.setPromptTokens(response.promptTokens());
        task.setCompletionTokens(response.completionTokens());
        task.setDurationMs(elapsedMillis(startNanos));
        task.setFinishedAt(LocalDateTime.now());
        aiTaskMapper.updateById(task);
    }

    private void failTask(AiTaskEntity task, String message, long startNanos) {
        task.setStatus("FAILED");
        task.setErrorMessage(truncate(message, 1000));
        task.setDurationMs(elapsedMillis(startNanos));
        task.setFinishedAt(LocalDateTime.now());
        try {
            aiTaskMapper.updateById(task);
        } catch (Exception persistenceException) {
            log.error("Failed to update AI task status, taskNo={}", task.getTaskNo(), persistenceException);
        }
    }

    private long elapsedMillis(long startNanos) {
        return Duration.ofNanos(System.nanoTime() - startNanos).toMillis();
    }

    private String stripMarkdownFence(String content) {
        String value = content == null ? "" : content.trim();
        if (value.startsWith("```")) {
            int firstLineEnd = value.indexOf('\n');
            int lastFence = value.lastIndexOf("```");
            if (firstLineEnd >= 0 && lastFence > firstLineEnd) {
                return value.substring(firstLineEnd + 1, lastFence).trim();
            }
        }
        return value;
    }

    private String sha256(String text) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
