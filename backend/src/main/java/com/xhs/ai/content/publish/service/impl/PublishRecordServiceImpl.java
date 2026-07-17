package com.xhs.ai.content.publish.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xhs.ai.content.common.api.PageResult;
import com.xhs.ai.content.common.exception.BizException;
import com.xhs.ai.content.common.exception.ErrorCode;
import com.xhs.ai.content.content.entity.ContentEntity;
import com.xhs.ai.content.content.enums.ContentStatus;
import com.xhs.ai.content.content.mapper.ContentMapper;
import com.xhs.ai.content.publish.dto.PublishRecordPageRequest;
import com.xhs.ai.content.publish.dto.PublishRecordSaveRequest;
import com.xhs.ai.content.publish.entity.PublishRecordEntity;
import com.xhs.ai.content.publish.mapper.PublishRecordMapper;
import com.xhs.ai.content.publish.service.PublishRecordService;
import com.xhs.ai.content.publish.vo.PublishRecordVO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class PublishRecordServiceImpl implements PublishRecordService {

    private final PublishRecordMapper publishRecordMapper;
    private final ContentMapper contentMapper;
    private final TransactionTemplate transactionTemplate;

    @Override
    public PublishRecordVO save(PublishRecordSaveRequest request) {
        ContentEntity content = requiredContent(request.contentId());
        ContentStatus status = ContentStatus.valueOf(content.getStatus());
        if (status != ContentStatus.READY_TO_PUBLISH && status != ContentStatus.PUBLISHED) {
            throw new BizException(ErrorCode.CONFLICT, "只有待发布内容才能登记发布记录");
        }
        Long recordId = Objects.requireNonNull(transactionTemplate.execute(tx -> {
            PublishRecordEntity record = publishRecordMapper.selectOne(
                    new LambdaQueryWrapper<PublishRecordEntity>()
                            .eq(PublishRecordEntity::getContentId, request.contentId()));
            if (record == null) {
                record = new PublishRecordEntity();
                record.setContentId(request.contentId());
                apply(record, request);
                publishRecordMapper.insert(record);
            } else {
                apply(record, request);
                ensureUpdated(publishRecordMapper.updateById(record));
            }
            if (status == ContentStatus.READY_TO_PUBLISH) {
                content.setStatus(ContentStatus.PUBLISHED.name());
                ensureUpdated(contentMapper.updateById(content));
            }
            return record.getId();
        }));
        return toVO(publishRecordMapper.selectById(recordId), content);
    }

    @Override
    public PageResult<PublishRecordVO> page(PublishRecordPageRequest request) {
        Page<PublishRecordEntity> page = publishRecordMapper.selectPage(
                new Page<>(request.current(), request.size()),
                new LambdaQueryWrapper<PublishRecordEntity>().orderByDesc(PublishRecordEntity::getPublishedAt));
        Map<Long, ContentEntity> contents = page.getRecords().isEmpty() ? Collections.emptyMap()
                : contentMapper.selectBatchIds(page.getRecords().stream()
                                .map(PublishRecordEntity::getContentId).distinct().toList())
                        .stream().collect(Collectors.toMap(ContentEntity::getId, Function.identity()));
        return PageResult.of(page, page.getRecords().stream()
                .map(record -> toVO(record, contents.get(record.getContentId())))
                .toList());
    }

    @Override
    public PublishRecordVO detail(Long id) {
        PublishRecordEntity record = publishRecordMapper.selectById(id);
        if (record == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "发布记录不存在");
        }
        return toVO(record, requiredContent(record.getContentId()));
    }

    private ContentEntity requiredContent(Long id) {
        ContentEntity content = contentMapper.selectById(id);
        if (content == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "内容不存在");
        }
        return content;
    }

    private void apply(PublishRecordEntity entity, PublishRecordSaveRequest request) {
        entity.setPublishedAt(request.publishedAt());
        entity.setNoteUrl(request.noteUrl());
        entity.setExposureCount(request.exposureCount());
        entity.setLikeCount(request.likeCount());
        entity.setFavoriteCount(request.favoriteCount());
        entity.setCommentCount(request.commentCount());
        entity.setFollowerGrowth(request.followerGrowth());
    }

    private PublishRecordVO toVO(PublishRecordEntity record, ContentEntity content) {
        long interactions = record.getLikeCount() + record.getFavoriteCount() + record.getCommentCount();
        return new PublishRecordVO(record.getId(), record.getContentId(),
                content == null ? "内容已不可见" : content.getSelectedTitle(), record.getPublishedAt(),
                record.getNoteUrl(), record.getExposureCount(), record.getLikeCount(), record.getFavoriteCount(),
                record.getCommentCount(), record.getFollowerGrowth(), rate(record.getFavoriteCount(), record.getExposureCount()),
                rate(interactions, record.getExposureCount()), record.getUpdatedAt());
    }

    private BigDecimal rate(long numerator, long denominator) {
        return denominator == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(numerator)
                .divide(BigDecimal.valueOf(denominator), 4, RoundingMode.HALF_UP);
    }

    private void ensureUpdated(int rows) {
        if (rows != 1) {
            throw new BizException(ErrorCode.CONFLICT, "数据已被其他操作修改，请刷新后重试");
        }
    }
}
