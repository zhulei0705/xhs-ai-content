package com.xhs.ai.content.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xhs.ai.content.ai.dto.AiGenerationRequest;
import com.xhs.ai.content.ai.dto.AiResult;
import com.xhs.ai.content.ai.dto.ContentReviewAiResponse;
import com.xhs.ai.content.ai.enums.AiOperation;
import com.xhs.ai.content.ai.prompt.PromptFactory;
import com.xhs.ai.content.ai.service.AiModelService;
import com.xhs.ai.content.common.exception.BizException;
import com.xhs.ai.content.common.exception.ErrorCode;
import com.xhs.ai.content.common.util.JsonCodec;
import com.xhs.ai.content.content.entity.ContentEntity;
import com.xhs.ai.content.content.enums.ContentStatus;
import com.xhs.ai.content.content.mapper.ContentMapper;
import com.xhs.ai.content.review.dto.ReviewConfirmRequest;
import com.xhs.ai.content.review.dto.ReviewExecuteRequest;
import com.xhs.ai.content.review.entity.AiReviewEntity;
import com.xhs.ai.content.review.enums.RiskLevel;
import com.xhs.ai.content.review.mapper.AiReviewMapper;
import com.xhs.ai.content.review.service.ReviewService;
import com.xhs.ai.content.review.vo.AiReviewVO;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ContentMapper contentMapper;
    private final AiReviewMapper reviewMapper;
    private final AiModelService aiModelService;
    private final PromptFactory promptFactory;
    private final JsonCodec jsonCodec;
    private final TransactionTemplate transactionTemplate;

    @Override
    public AiReviewVO execute(ReviewExecuteRequest request) {
        ContentEntity content = requiredContent(request.contentId());
        ContentStatus current = ContentStatus.valueOf(content.getStatus());
        if (current == ContentStatus.PUBLISHED || current == ContentStatus.ARCHIVED) {
            throw new BizException(ErrorCode.CONFLICT, "已发布或已归档内容不能重新审核");
        }

        Map<String, Object> context = new LinkedHashMap<>();
        context.put("title", content.getSelectedTitle());
        context.put("body", content.getBody());
        context.put("coverTitle", content.getCoverTitle());
        context.put("coverSubtitle", content.getCoverSubtitle());
        context.put("tags", jsonCodec.readStringList(content.getTagsJson()));
        context.put("interactionGuide", content.getInteractionGuide());

        AiOperation operation = AiOperation.CONTENT_REVIEW;
        AiResult<ContentReviewAiResponse> aiResult = aiModelService.generateStructured(
                new AiGenerationRequest(operation, operation.getPromptVersion(), content.getId(),
                        promptFactory.systemPrompt(), promptFactory.reviewPrompt(context)),
                ContentReviewAiResponse.class);
        validate(aiResult.data());

        Long reviewId = Objects.requireNonNull(transactionTemplate.execute(status -> {
            AiReviewEntity review = fromAi(content.getId(), aiResult);
            reviewMapper.insert(review);
            content.setCurrentReviewId(review.getId());
            content.setStatus(ContentStatus.PENDING_REVIEW.name());
            ensureUpdated(contentMapper.updateById(content));
            return review.getId();
        }));
        return toVO(reviewMapper.selectById(reviewId));
    }

    @Override
    public List<AiReviewVO> history(Long contentId) {
        requiredContent(contentId);
        return reviewMapper.selectList(new LambdaQueryWrapper<AiReviewEntity>()
                        .eq(AiReviewEntity::getContentId, contentId)
                        .orderByDesc(AiReviewEntity::getReviewedAt))
                .stream().map(this::toVO).toList();
    }

    @Override
    public void confirm(ReviewConfirmRequest request) {
        ContentEntity content = requiredContent(request.contentId());
        if (!ContentStatus.PENDING_REVIEW.name().equals(content.getStatus())
                || content.getCurrentReviewId() == null) {
            throw new BizException(ErrorCode.CONFLICT, "只有待审核内容可以人工确认");
        }
        AiReviewEntity review = reviewMapper.selectById(content.getCurrentReviewId());
        if (review == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "审核记录不存在");
        }
        if (Boolean.TRUE.equals(request.approved()) && RiskLevel.HIGH.name().equals(review.getRiskLevel())) {
            throw new BizException(ErrorCode.CONFLICT, "高风险内容不能直接通过，请修改后重新审核");
        }
        content.setStatus(Boolean.TRUE.equals(request.approved())
                ? ContentStatus.READY_TO_PUBLISH.name() : ContentStatus.DRAFT.name());
        ensureUpdated(contentMapper.updateById(content));
    }

    private ContentEntity requiredContent(Long id) {
        ContentEntity content = contentMapper.selectById(id);
        if (content == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "内容不存在");
        }
        return content;
    }

    private AiReviewEntity fromAi(Long contentId, AiResult<ContentReviewAiResponse> result) {
        ContentReviewAiResponse response = result.data();
        AiReviewEntity entity = new AiReviewEntity();
        entity.setContentId(contentId);
        entity.setAiToneScore(response.aiToneScore());
        entity.setTitleAttractionScore(response.titleAttractionScore());
        entity.setAuthenticityScore(response.authenticityScore());
        entity.setPlatformFitScore(response.platformFitScore());
        entity.setExaggeratedIncomeRisk(response.exaggeratedIncomeRisk());
        entity.setSensitiveExpressionRisk(response.sensitiveExpressionRisk());
        entity.setMarketingRisk(response.marketingRisk());
        entity.setRiskLevel(response.riskLevel());
        entity.setSummary(response.summary());
        entity.setSuggestionsJson(jsonCodec.write(response.suggestions()));
        entity.setProvider(result.provider());
        entity.setModelName(result.model());
        entity.setReviewedAt(LocalDateTime.now());
        return entity;
    }

    private void validate(ContentReviewAiResponse response) {
        if (response == null || !validScore(response.aiToneScore())
                || !validScore(response.titleAttractionScore()) || !validScore(response.authenticityScore())
                || !validScore(response.platformFitScore()) || response.exaggeratedIncomeRisk() == null
                || response.sensitiveExpressionRisk() == null || response.marketingRisk() == null
                || !StringUtils.hasText(response.summary()) || response.suggestions() == null) {
            throw new BizException(ErrorCode.AI_RESPONSE_INVALID, "AI审核结果字段不完整或评分越界");
        }
        try {
            RiskLevel.valueOf(response.riskLevel());
        } catch (Exception exception) {
            throw new BizException(ErrorCode.AI_RESPONSE_INVALID, "AI审核风险等级不正确");
        }
    }

    private boolean validScore(Integer score) {
        return score != null && score >= 0 && score <= 100;
    }

    private AiReviewVO toVO(AiReviewEntity review) {
        return new AiReviewVO(review.getId(), review.getContentId(), review.getAiToneScore(),
                review.getTitleAttractionScore(), review.getAuthenticityScore(), review.getPlatformFitScore(),
                review.getExaggeratedIncomeRisk(), review.getSensitiveExpressionRisk(), review.getMarketingRisk(),
                review.getRiskLevel(), review.getSummary(), jsonCodec.readStringList(review.getSuggestionsJson()),
                review.getProvider(), review.getModelName(), review.getReviewedAt());
    }

    private void ensureUpdated(int rows) {
        if (rows != 1) {
            throw new BizException(ErrorCode.CONFLICT, "数据已被其他操作修改，请刷新后重试");
        }
    }
}
