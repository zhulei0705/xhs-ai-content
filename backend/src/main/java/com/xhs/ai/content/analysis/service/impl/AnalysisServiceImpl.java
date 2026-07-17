package com.xhs.ai.content.analysis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xhs.ai.content.ai.dto.AiGenerationRequest;
import com.xhs.ai.content.ai.dto.AiResult;
import com.xhs.ai.content.ai.dto.PerformanceAnalysisAiResponse;
import com.xhs.ai.content.ai.enums.AiOperation;
import com.xhs.ai.content.ai.prompt.PromptFactory;
import com.xhs.ai.content.ai.service.AiModelService;
import com.xhs.ai.content.analysis.dto.AnalysisExecuteRequest;
import com.xhs.ai.content.analysis.dto.DashboardRequest;
import com.xhs.ai.content.analysis.entity.PerformanceAnalysisEntity;
import com.xhs.ai.content.analysis.mapper.PerformanceAnalysisMapper;
import com.xhs.ai.content.analysis.service.AnalysisService;
import com.xhs.ai.content.analysis.vo.DashboardVO;
import com.xhs.ai.content.analysis.vo.PerformanceAnalysisVO;
import com.xhs.ai.content.common.exception.BizException;
import com.xhs.ai.content.common.exception.ErrorCode;
import com.xhs.ai.content.common.util.JsonCodec;
import com.xhs.ai.content.content.entity.ContentEntity;
import com.xhs.ai.content.content.mapper.ContentMapper;
import com.xhs.ai.content.publish.entity.PublishRecordEntity;
import com.xhs.ai.content.publish.mapper.PublishRecordMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalysisServiceImpl implements AnalysisService {

    private final PerformanceAnalysisMapper analysisMapper;
    private final PublishRecordMapper publishRecordMapper;
    private final ContentMapper contentMapper;
    private final AiModelService aiModelService;
    private final PromptFactory promptFactory;
    private final JsonCodec jsonCodec;

    @Override
    public PerformanceAnalysisVO execute(AnalysisExecuteRequest request) {
        PublishRecordEntity record = requiredRecord(request.publishRecordId());
        ContentEntity content = contentMapper.selectById(record.getContentId());
        if (content == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "发布内容不存在");
        }
        Map<String, Object> metrics = metrics(record, content);
        AiOperation operation = AiOperation.PERFORMANCE_ANALYSIS;
        AiResult<PerformanceAnalysisAiResponse> result = aiModelService.generateStructured(
                new AiGenerationRequest(operation, operation.getPromptVersion(), record.getId(),
                        promptFactory.systemPrompt(), promptFactory.analysisPrompt(metrics)),
                PerformanceAnalysisAiResponse.class);
        validate(result.data());
        PerformanceAnalysisEntity entity = fromAi(record.getId(), result);
        analysisMapper.insert(entity);
        return toVO(entity);
    }

    @Override
    public PerformanceAnalysisVO latest(Long publishRecordId) {
        requiredRecord(publishRecordId);
        PerformanceAnalysisEntity entity = analysisMapper.selectOne(
                new LambdaQueryWrapper<PerformanceAnalysisEntity>()
                        .eq(PerformanceAnalysisEntity::getPublishRecordId, publishRecordId)
                        .orderByDesc(PerformanceAnalysisEntity::getAnalyzedAt)
                        .last("LIMIT 1"));
        if (entity == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "尚未生成复盘");
        }
        return toVO(entity);
    }

    @Override
    public DashboardVO dashboard(DashboardRequest request) {
        LocalDate endDate = request.endDate() == null ? LocalDate.now() : request.endDate();
        LocalDate startDate = request.startDate() == null ? endDate.minusDays(29) : request.startDate();
        if (startDate.isAfter(endDate)) {
            throw new BizException(ErrorCode.INVALID_PARAMETER, "开始日期不能晚于结束日期");
        }
        List<PublishRecordEntity> records = publishRecordMapper.selectList(
                new LambdaQueryWrapper<PublishRecordEntity>()
                        .between(PublishRecordEntity::getPublishedAt,
                                startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX)));
        long exposure = records.stream().mapToLong(PublishRecordEntity::getExposureCount).sum();
        long likes = records.stream().mapToLong(PublishRecordEntity::getLikeCount).sum();
        long favorites = records.stream().mapToLong(PublishRecordEntity::getFavoriteCount).sum();
        long comments = records.stream().mapToLong(PublishRecordEntity::getCommentCount).sum();
        long followers = records.stream().mapToLong(PublishRecordEntity::getFollowerGrowth).sum();
        List<DashboardVO.TopContent> topContents = records.stream()
                .sorted(Comparator.comparingLong(PublishRecordEntity::getExposureCount).reversed())
                .limit(5)
                .map(record -> {
                    ContentEntity content = contentMapper.selectById(record.getContentId());
                    long interactions = record.getLikeCount() + record.getFavoriteCount() + record.getCommentCount();
                    return new DashboardVO.TopContent(record.getContentId(),
                            content == null ? "内容已不可见" : content.getSelectedTitle(), record.getExposureCount(),
                            rate(interactions, record.getExposureCount()));
                }).toList();
        return new DashboardVO(records.size(), exposure, likes, favorites, comments, followers,
                rate(favorites, exposure), rate(likes + favorites + comments, exposure), topContents);
    }

    private Map<String, Object> metrics(PublishRecordEntity record, ContentEntity content) {
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("title", content.getSelectedTitle());
        metrics.put("tags", jsonCodec.readStringList(content.getTagsJson()));
        metrics.put("publishedAt", record.getPublishedAt());
        metrics.put("exposure", record.getExposureCount());
        metrics.put("likes", record.getLikeCount());
        metrics.put("favorites", record.getFavoriteCount());
        metrics.put("comments", record.getCommentCount());
        metrics.put("followerGrowth", record.getFollowerGrowth());
        metrics.put("favoriteRate", rate(record.getFavoriteCount(), record.getExposureCount()));
        metrics.put("interactionRate", rate(
                record.getLikeCount() + record.getFavoriteCount() + record.getCommentCount(),
                record.getExposureCount()));
        return metrics;
    }

    private PublishRecordEntity requiredRecord(Long id) {
        PublishRecordEntity record = publishRecordMapper.selectById(id);
        if (record == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "发布记录不存在");
        }
        return record;
    }

    private PerformanceAnalysisEntity fromAi(Long recordId, AiResult<PerformanceAnalysisAiResponse> result) {
        PerformanceAnalysisAiResponse response = result.data();
        PerformanceAnalysisEntity entity = new PerformanceAnalysisEntity();
        entity.setPublishRecordId(recordId);
        entity.setOverallGrade(response.overallGrade());
        entity.setPerformanceSummary(response.performanceSummary());
        entity.setTopicAnalysis(response.topicAnalysis());
        entity.setTitleAnalysis(response.titleAnalysis());
        entity.setFavoriteRateAnalysis(response.favoriteRateAnalysis());
        entity.setInteractionRateAnalysis(response.interactionRateAnalysis());
        entity.setNextTopicSuggestionsJson(jsonCodec.write(response.nextTopicSuggestions()));
        entity.setProvider(result.provider());
        entity.setModelName(result.model());
        entity.setAnalyzedAt(LocalDateTime.now());
        return entity;
    }

    private void validate(PerformanceAnalysisAiResponse response) {
        if (response == null || response.overallGrade() == null || response.performanceSummary() == null
                || response.topicAnalysis() == null || response.titleAnalysis() == null
                || response.favoriteRateAnalysis() == null || response.interactionRateAnalysis() == null
                || response.nextTopicSuggestions() == null || response.nextTopicSuggestions().isEmpty()) {
            throw new BizException(ErrorCode.AI_RESPONSE_INVALID, "AI复盘结果字段不完整");
        }
    }

    private PerformanceAnalysisVO toVO(PerformanceAnalysisEntity entity) {
        return new PerformanceAnalysisVO(entity.getId(), entity.getPublishRecordId(), entity.getOverallGrade(),
                entity.getPerformanceSummary(), entity.getTopicAnalysis(), entity.getTitleAnalysis(),
                entity.getFavoriteRateAnalysis(), entity.getInteractionRateAnalysis(),
                jsonCodec.readStringList(entity.getNextTopicSuggestionsJson()), entity.getProvider(),
                entity.getModelName(), entity.getAnalyzedAt());
    }

    private BigDecimal rate(long numerator, long denominator) {
        return denominator == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(numerator)
                .divide(BigDecimal.valueOf(denominator), 4, RoundingMode.HALF_UP);
    }
}
