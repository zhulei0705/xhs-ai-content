package com.xhs.ai.content.topic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xhs.ai.content.ai.dto.AiGenerationRequest;
import com.xhs.ai.content.ai.dto.AiResult;
import com.xhs.ai.content.ai.dto.TopicBatchAiResponse;
import com.xhs.ai.content.ai.enums.AiOperation;
import com.xhs.ai.content.ai.prompt.PromptFactory;
import com.xhs.ai.content.ai.service.AiModelService;
import com.xhs.ai.content.common.api.PageResult;
import com.xhs.ai.content.common.exception.BizException;
import com.xhs.ai.content.common.exception.ErrorCode;
import com.xhs.ai.content.common.util.JsonCodec;
import com.xhs.ai.content.topic.dto.TopicCreateRequest;
import com.xhs.ai.content.topic.dto.TopicGenerateRequest;
import com.xhs.ai.content.topic.dto.TopicPageRequest;
import com.xhs.ai.content.topic.dto.TopicStatusUpdateRequest;
import com.xhs.ai.content.topic.dto.TopicUpdateRequest;
import com.xhs.ai.content.topic.entity.TopicEntity;
import com.xhs.ai.content.topic.enums.TopicStatus;
import com.xhs.ai.content.topic.mapper.TopicMapper;
import com.xhs.ai.content.topic.service.TopicService;
import com.xhs.ai.content.topic.vo.TopicVO;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class TopicServiceImpl implements TopicService {

    private final TopicMapper topicMapper;
    private final AiModelService aiModelService;
    private final PromptFactory promptFactory;
    private final JsonCodec jsonCodec;
    private final TransactionTemplate transactionTemplate;

    @Override
    public List<TopicVO> generate(TopicGenerateRequest request) {
        LocalDate date = request.actualDate();
        List<TopicEntity> existing = listByDate(date);
        if (!existing.isEmpty()) {
            return existing.stream().map(this::toVO).toList();
        }

        int count = request.actualCount();
        AiOperation operation = AiOperation.TOPIC_GENERATION;
        AiResult<TopicBatchAiResponse> aiResult = aiModelService.generateStructured(
                new AiGenerationRequest(operation, operation.getPromptVersion(), null,
                        promptFactory.systemPrompt(), promptFactory.topicPrompt(count, date)),
                TopicBatchAiResponse.class);
        validateAiTopics(aiResult.data(), count);

        List<TopicEntity> entities = aiResult.data().topics().stream()
                .map(item -> fromAi(item, date))
                .toList();
        return Objects.requireNonNull(transactionTemplate.execute(status -> {
            entities.forEach(topicMapper::insert);
            return entities.stream().map(this::toVO).toList();
        }));
    }

    @Override
    public PageResult<TopicVO> page(TopicPageRequest request) {
        LambdaQueryWrapper<TopicEntity> query = new LambdaQueryWrapper<>();
        query.eq(StringUtils.hasText(request.status()), TopicEntity::getStatus, request.status())
                .eq(request.generationDate() != null, TopicEntity::getGenerationDate, request.generationDate())
                .like(StringUtils.hasText(request.keyword()), TopicEntity::getTopicTitle, request.keyword())
                .orderByDesc(TopicEntity::getGenerationDate, TopicEntity::getOverallScore)
                .orderByAsc(TopicEntity::getPublishPriority);
        Page<TopicEntity> page = topicMapper.selectPage(new Page<>(request.current(), request.size()), query);
        return PageResult.of(page, page.getRecords().stream().map(this::toVO).toList());
    }

    @Override
    public TopicVO detail(Long id) {
        return toVO(required(id));
    }

    @Override
    public TopicVO create(TopicCreateRequest request) {
        TopicEntity entity = new TopicEntity();
        apply(entity, request);
        entity.setSourceType("MANUAL");
        entity.setGenerationDate(request.generationDate() == null ? LocalDate.now() : request.generationDate());
        entity.setStatus(TopicStatus.CANDIDATE.name());
        topicMapper.insert(entity);
        return toVO(entity);
    }

    @Override
    public TopicVO update(TopicUpdateRequest request) {
        TopicEntity entity = required(request.id());
        if (TopicStatus.USED.name().equals(entity.getStatus())) {
            throw new BizException(ErrorCode.CONFLICT, "已生成内容的选题不能修改");
        }
        entity.setTopicTitle(request.topicTitle());
        entity.setCoreAngle(request.coreAngle());
        entity.setTargetAudience(request.targetAudience());
        entity.setContentType(request.contentType());
        entity.setKeywordsJson(jsonCodec.write(request.keywords()));
        entity.setTargetMatchScore(request.targetMatchScore());
        entity.setViralPotentialScore(request.viralPotentialScore());
        entity.setOverallScore(request.overallScore());
        entity.setPotentialAnalysis(request.potentialAnalysis());
        entity.setPublishPriority(request.publishPriority());
        ensureUpdated(topicMapper.updateById(entity));
        return toVO(entity);
    }

    @Override
    public void delete(Long id) {
        TopicEntity entity = required(id);
        if (TopicStatus.USED.name().equals(entity.getStatus())) {
            throw new BizException(ErrorCode.CONFLICT, "已使用选题不能删除，可改为归档");
        }
        topicMapper.deleteById(entity);
    }

    @Override
    public void updateStatus(TopicStatusUpdateRequest request) {
        TopicEntity entity = required(request.id());
        if (TopicStatus.USED.name().equals(entity.getStatus()) && request.status() != TopicStatus.ARCHIVED) {
            throw new BizException(ErrorCode.CONFLICT, "已使用选题只能归档");
        }
        entity.setStatus(request.status().name());
        ensureUpdated(topicMapper.updateById(entity));
    }

    @Override
    public boolean existsGeneratedOn(LocalDate date) {
        return topicMapper.selectCount(new LambdaQueryWrapper<TopicEntity>()
                .eq(TopicEntity::getGenerationDate, date)
                .eq(TopicEntity::getSourceType, "AI")) > 0;
    }

    private List<TopicEntity> listByDate(LocalDate date) {
        return topicMapper.selectList(new LambdaQueryWrapper<TopicEntity>()
                .eq(TopicEntity::getGenerationDate, date)
                .eq(TopicEntity::getSourceType, "AI")
                .orderByAsc(TopicEntity::getPublishPriority));
    }

    private TopicEntity required(Long id) {
        TopicEntity entity = topicMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "选题不存在");
        }
        return entity;
    }

    private TopicEntity fromAi(TopicBatchAiResponse.TopicItem item, LocalDate date) {
        TopicEntity entity = new TopicEntity();
        entity.setTopicTitle(item.topicTitle());
        entity.setCoreAngle(item.coreAngle());
        entity.setTargetAudience(item.targetAudience());
        entity.setContentType(item.contentType());
        entity.setKeywordsJson(jsonCodec.write(item.keywords()));
        entity.setTargetMatchScore(item.targetMatchScore());
        entity.setViralPotentialScore(item.viralPotentialScore());
        entity.setOverallScore(item.overallScore());
        entity.setPotentialAnalysis(item.potentialAnalysis());
        entity.setPublishPriority(item.publishPriority());
        entity.setSourceType("AI");
        entity.setGenerationDate(date);
        entity.setStatus(TopicStatus.CANDIDATE.name());
        return entity;
    }

    private void apply(TopicEntity entity, TopicCreateRequest request) {
        entity.setTopicTitle(request.topicTitle());
        entity.setCoreAngle(request.coreAngle());
        entity.setTargetAudience(request.targetAudience());
        entity.setContentType(request.contentType());
        entity.setKeywordsJson(jsonCodec.write(request.keywords()));
        entity.setTargetMatchScore(request.targetMatchScore());
        entity.setViralPotentialScore(request.viralPotentialScore());
        entity.setOverallScore(request.overallScore());
        entity.setPotentialAnalysis(request.potentialAnalysis());
        entity.setPublishPriority(request.publishPriority());
    }

    private void validateAiTopics(TopicBatchAiResponse response, int expectedCount) {
        if (response == null || response.topics() == null || response.topics().size() != expectedCount) {
            throw new BizException(ErrorCode.AI_RESPONSE_INVALID,
                    "AI必须返回" + expectedCount + "个选题");
        }
        for (TopicBatchAiResponse.TopicItem item : response.topics()) {
            if (!StringUtils.hasText(item.topicTitle()) || !StringUtils.hasText(item.coreAngle())
                    || item.keywords() == null || item.keywords().isEmpty()
                    || !validScore(item.targetMatchScore()) || !validScore(item.viralPotentialScore())
                    || !validScore(item.overallScore()) || item.publishPriority() == null
                    || item.publishPriority() < 1 || item.publishPriority() > expectedCount) {
                throw new BizException(ErrorCode.AI_RESPONSE_INVALID, "AI选题字段不完整或评分越界");
            }
        }
        long priorities = response.topics().stream().map(TopicBatchAiResponse.TopicItem::publishPriority)
                .distinct().count();
        if (priorities != expectedCount) {
            throw new BizException(ErrorCode.AI_RESPONSE_INVALID, "AI选题优先级必须唯一");
        }
    }

    private boolean validScore(Integer score) {
        return score != null && score >= 0 && score <= 100;
    }

    private void ensureUpdated(int rows) {
        if (rows != 1) {
            throw new BizException(ErrorCode.CONFLICT, "数据已被其他操作修改，请刷新后重试");
        }
    }

    private TopicVO toVO(TopicEntity entity) {
        return new TopicVO(entity.getId(), entity.getTopicTitle(), entity.getCoreAngle(),
                entity.getTargetAudience(), entity.getContentType(), jsonCodec.readStringList(entity.getKeywordsJson()),
                entity.getTargetMatchScore(), entity.getViralPotentialScore(), entity.getOverallScore(),
                entity.getPotentialAnalysis(), entity.getPublishPriority(), entity.getSourceType(),
                entity.getGenerationDate(), entity.getStatus(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
