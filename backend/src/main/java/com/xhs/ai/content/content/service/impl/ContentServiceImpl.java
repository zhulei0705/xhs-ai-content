package com.xhs.ai.content.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xhs.ai.content.ai.dto.AiGenerationRequest;
import com.xhs.ai.content.ai.dto.AiResult;
import com.xhs.ai.content.ai.dto.ContentPackageAiResponse;
import com.xhs.ai.content.ai.enums.AiOperation;
import com.xhs.ai.content.ai.prompt.PromptFactory;
import com.xhs.ai.content.ai.service.AiModelService;
import com.xhs.ai.content.common.api.PageResult;
import com.xhs.ai.content.common.exception.BizException;
import com.xhs.ai.content.common.exception.ErrorCode;
import com.xhs.ai.content.common.util.JsonCodec;
import com.xhs.ai.content.content.dto.ContentGenerateRequest;
import com.xhs.ai.content.content.dto.ContentPageRequest;
import com.xhs.ai.content.content.dto.ContentSaveRequest;
import com.xhs.ai.content.content.dto.ContentStatusUpdateRequest;
import com.xhs.ai.content.content.entity.ContentCardEntity;
import com.xhs.ai.content.content.entity.ContentEntity;
import com.xhs.ai.content.content.entity.ContentTitleEntity;
import com.xhs.ai.content.content.enums.ContentStatus;
import com.xhs.ai.content.content.mapper.ContentCardMapper;
import com.xhs.ai.content.content.mapper.ContentMapper;
import com.xhs.ai.content.content.mapper.ContentTitleMapper;
import com.xhs.ai.content.content.service.ContentService;
import com.xhs.ai.content.content.vo.ContentCopyVO;
import com.xhs.ai.content.content.vo.ContentDetailVO;
import com.xhs.ai.content.content.vo.ContentListVO;
import com.xhs.ai.content.review.entity.AiReviewEntity;
import com.xhs.ai.content.review.mapper.AiReviewMapper;
import com.xhs.ai.content.review.vo.AiReviewVO;
import com.xhs.ai.content.topic.entity.TopicEntity;
import com.xhs.ai.content.topic.enums.TopicStatus;
import com.xhs.ai.content.topic.mapper.TopicMapper;
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
public class ContentServiceImpl implements ContentService {

    private final ContentMapper contentMapper;
    private final ContentTitleMapper contentTitleMapper;
    private final ContentCardMapper contentCardMapper;
    private final TopicMapper topicMapper;
    private final AiReviewMapper reviewMapper;
    private final AiModelService aiModelService;
    private final PromptFactory promptFactory;
    private final JsonCodec jsonCodec;
    private final TransactionTemplate transactionTemplate;

    @Override
    public ContentDetailVO generate(ContentGenerateRequest request) {
        TopicEntity topic = topicMapper.selectById(request.topicId());
        if (topic == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "选题不存在");
        }
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("topicTitle", topic.getTopicTitle());
        context.put("coreAngle", topic.getCoreAngle());
        context.put("targetAudience", topic.getTargetAudience());
        context.put("contentType", topic.getContentType());
        context.put("keywords", jsonCodec.readStringList(topic.getKeywordsJson()));

        AiOperation operation = AiOperation.CONTENT_GENERATION;
        AiResult<ContentPackageAiResponse> aiResult = aiModelService.generateStructured(
                new AiGenerationRequest(operation, operation.getPromptVersion(), topic.getId(),
                        promptFactory.systemPrompt(), promptFactory.contentPrompt(context)),
                ContentPackageAiResponse.class);
        validateAiContent(aiResult.data());

        Long contentId = Objects.requireNonNull(transactionTemplate.execute(status -> {
            ContentEntity content = fromAi(aiResult.data(), topic.getId());
            contentMapper.insert(content);
            saveAiChildren(content.getId(), content.getSelectedTitle(), aiResult.data());
            topic.setStatus(TopicStatus.USED.name());
            topicMapper.updateById(topic);
            return content.getId();
        }));
        return detail(contentId);
    }

    @Override
    public PageResult<ContentListVO> page(ContentPageRequest request) {
        LambdaQueryWrapper<ContentEntity> query = new LambdaQueryWrapper<>();
        query.eq(StringUtils.hasText(request.status()), ContentEntity::getStatus, request.status())
                .like(StringUtils.hasText(request.keyword()), ContentEntity::getSelectedTitle, request.keyword())
                .orderByDesc(ContentEntity::getUpdatedAt);
        Page<ContentEntity> page = contentMapper.selectPage(new Page<>(request.current(), request.size()), query);
        return PageResult.of(page, page.getRecords().stream().map(this::toListVO).toList());
    }

    @Override
    public ContentDetailVO detail(Long id) {
        ContentEntity content = required(id);
        List<ContentTitleEntity> titles = contentTitleMapper.selectList(
                new LambdaQueryWrapper<ContentTitleEntity>()
                        .eq(ContentTitleEntity::getContentId, id)
                        .orderByAsc(ContentTitleEntity::getSortOrder));
        List<ContentCardEntity> cards = contentCardMapper.selectList(
                new LambdaQueryWrapper<ContentCardEntity>()
                        .eq(ContentCardEntity::getContentId, id)
                        .orderByAsc(ContentCardEntity::getCardNo));
        AiReviewVO review = content.getCurrentReviewId() == null
                ? null : toReviewVO(reviewMapper.selectById(content.getCurrentReviewId()));
        return new ContentDetailVO(content.getId(), content.getTopicId(), content.getSelectedTitle(),
                content.getBody(), content.getCoverTitle(), content.getCoverSubtitle(),
                jsonCodec.readStringList(content.getTagsJson()), content.getInteractionGuide(), content.getStatus(),
                titles.stream().map(title -> new ContentDetailVO.TitleVO(title.getId(), title.getTitleText(),
                        title.getSortOrder(), title.getSelected(), title.getAttractionScore())).toList(),
                cards.stream().map(card -> new ContentDetailVO.CardVO(card.getId(), card.getCardNo(),
                        card.getCardTitle(), card.getCardBody())).toList(),
                review, content.getCreatedAt(), content.getUpdatedAt());
    }

    @Override
    public ContentDetailVO create(ContentSaveRequest request) {
        Long contentId = Objects.requireNonNull(transactionTemplate.execute(status -> {
            ContentEntity content = fromRequest(request);
            content.setStatus(ContentStatus.DRAFT.name());
            contentMapper.insert(content);
            saveRequestChildren(content.getId(), request);
            return content.getId();
        }));
        return detail(contentId);
    }

    @Override
    public ContentDetailVO update(ContentSaveRequest request) {
        if (request.id() == null) {
            throw new BizException(ErrorCode.INVALID_PARAMETER, "内容ID不能为空");
        }
        transactionTemplate.executeWithoutResult(status -> {
            ContentEntity content = required(request.id());
            ContentStatus current = ContentStatus.valueOf(content.getStatus());
            if (current == ContentStatus.PUBLISHED || current == ContentStatus.ARCHIVED) {
                throw new BizException(ErrorCode.CONFLICT, "已发布或已归档内容不能编辑");
            }
            content.setTopicId(request.topicId());
            content.setSelectedTitle(request.selectedTitle());
            content.setBody(request.body());
            content.setCoverTitle(request.coverTitle());
            content.setCoverSubtitle(request.coverSubtitle());
            content.setTagsJson(jsonCodec.write(request.tags()));
            content.setInteractionGuide(request.interactionGuide());
            ensureUpdated(contentMapper.updateById(content));
            contentTitleMapper.deletePhysicalByContentId(content.getId());
            contentCardMapper.deletePhysicalByContentId(content.getId());
            saveRequestChildren(content.getId(), request);
        });
        return detail(request.id());
    }

    @Override
    public void delete(Long id) {
        ContentEntity content = required(id);
        ContentStatus status = ContentStatus.valueOf(content.getStatus());
        if (status == ContentStatus.PUBLISHED || status == ContentStatus.ARCHIVED) {
            throw new BizException(ErrorCode.CONFLICT, "已发布或已归档内容不能删除");
        }
        contentMapper.deleteById(content);
    }

    @Override
    public void updateStatus(ContentStatusUpdateRequest request) {
        ContentEntity content = required(request.id());
        ContentStatus current = ContentStatus.valueOf(content.getStatus());
        ContentStatus target = request.targetStatus();
        if (!current.canTransitionTo(target)) {
            throw new BizException(ErrorCode.CONFLICT,
                    "不允许从" + current + "变更为" + target);
        }
        if (target == ContentStatus.READY_TO_PUBLISH) {
            throw new BizException(ErrorCode.CONFLICT, "请通过人工审核确认接口将内容设为待发布");
        }
        if (target == ContentStatus.PUBLISHED) {
            throw new BizException(ErrorCode.CONFLICT, "请通过发布记录接口将内容标记为已发布");
        }
        content.setStatus(target.name());
        ensureUpdated(contentMapper.updateById(content));
    }

    @Override
    public ContentCopyVO copy(Long id) {
        ContentEntity content = required(id);
        String tags = jsonCodec.readStringList(content.getTagsJson()).stream()
                .map(tag -> "#" + tag.replaceFirst("^#", ""))
                .reduce((left, right) -> left + " " + right)
                .orElse("");
        String bodyWithTags = content.getBody() + (tags.isEmpty() ? "" : "\n\n" + tags);
        return new ContentCopyVO(content.getId(), content.getSelectedTitle(), bodyWithTags,
                content.getInteractionGuide());
    }

    private ContentEntity required(Long id) {
        ContentEntity content = contentMapper.selectById(id);
        if (content == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "内容不存在");
        }
        return content;
    }

    private ContentEntity fromAi(ContentPackageAiResponse response, Long topicId) {
        ContentEntity entity = new ContentEntity();
        entity.setTopicId(topicId);
        entity.setSelectedTitle(response.titles().getFirst().text());
        entity.setBody(response.body());
        entity.setCoverTitle(response.coverTitle());
        entity.setCoverSubtitle(response.coverSubtitle());
        entity.setTagsJson(jsonCodec.write(response.tags()));
        entity.setInteractionGuide(response.interactionGuide());
        entity.setStatus(ContentStatus.DRAFT.name());
        return entity;
    }

    private ContentEntity fromRequest(ContentSaveRequest request) {
        ContentEntity entity = new ContentEntity();
        entity.setTopicId(request.topicId());
        entity.setSelectedTitle(request.selectedTitle());
        entity.setBody(request.body());
        entity.setCoverTitle(request.coverTitle());
        entity.setCoverSubtitle(request.coverSubtitle());
        entity.setTagsJson(jsonCodec.write(request.tags()));
        entity.setInteractionGuide(request.interactionGuide());
        return entity;
    }

    private void saveAiChildren(Long contentId, String selectedTitle, ContentPackageAiResponse response) {
        for (int index = 0; index < response.titles().size(); index++) {
            ContentPackageAiResponse.TitleItem item = response.titles().get(index);
            saveTitle(contentId, item.text(), index + 1, item.text().equals(selectedTitle), item.attractionScore());
        }
        response.cards().forEach(item -> saveCard(contentId, item.cardNo(), item.title(), item.body()));
    }

    private void saveRequestChildren(Long contentId, ContentSaveRequest request) {
        request.titles().forEach(item -> saveTitle(contentId, item.text(), item.sortOrder(),
                item.text().equals(request.selectedTitle()), item.attractionScore()));
        request.cards().forEach(item -> saveCard(contentId, item.cardNo(), item.title(), item.body()));
    }

    private void saveTitle(Long contentId, String text, Integer order, boolean selected, Integer score) {
        ContentTitleEntity title = new ContentTitleEntity();
        title.setContentId(contentId);
        title.setTitleText(text);
        title.setSortOrder(order);
        title.setSelected(selected);
        title.setAttractionScore(score);
        contentTitleMapper.insert(title);
    }

    private void saveCard(Long contentId, Integer cardNo, String titleText, String body) {
        ContentCardEntity card = new ContentCardEntity();
        card.setContentId(contentId);
        card.setCardNo(cardNo);
        card.setCardTitle(titleText);
        card.setCardBody(body);
        contentCardMapper.insert(card);
    }

    private void validateAiContent(ContentPackageAiResponse response) {
        if (response == null || response.titles() == null || response.titles().size() != 5
                || response.cards() == null || response.cards().size() != 6
                || response.tags() == null || response.tags().isEmpty()
                || !StringUtils.hasText(response.body()) || !StringUtils.hasText(response.coverTitle())
                || !StringUtils.hasText(response.coverSubtitle()) || !StringUtils.hasText(response.interactionGuide())) {
            throw new BizException(ErrorCode.AI_RESPONSE_INVALID,
                    "AI内容包必须包含5个标题、正文、封面、6张卡片、标签和互动语");
        }
        boolean invalidTitle = response.titles().stream()
                .anyMatch(title -> !StringUtils.hasText(title.text()) || title.attractionScore() == null
                        || title.attractionScore() < 0 || title.attractionScore() > 100);
        boolean invalidCard = response.cards().stream()
                .anyMatch(card -> card.cardNo() == null || card.cardNo() < 1 || card.cardNo() > 6
                        || !StringUtils.hasText(card.title()) || !StringUtils.hasText(card.body()));
        if (invalidTitle || invalidCard) {
            throw new BizException(ErrorCode.AI_RESPONSE_INVALID, "AI标题或图文卡片字段不完整");
        }
        long cardNumbers = response.cards().stream().map(ContentPackageAiResponse.CardItem::cardNo)
                .filter(Objects::nonNull).distinct().count();
        if (cardNumbers != 6) {
            throw new BizException(ErrorCode.AI_RESPONSE_INVALID, "图文卡片编号必须为1到6且不能重复");
        }
    }

    private ContentListVO toListVO(ContentEntity content) {
        return new ContentListVO(content.getId(), content.getTopicId(), content.getSelectedTitle(),
                content.getCoverTitle(), jsonCodec.readStringList(content.getTagsJson()), content.getStatus(),
                content.getCreatedAt(), content.getUpdatedAt());
    }

    private AiReviewVO toReviewVO(AiReviewEntity review) {
        if (review == null) {
            return null;
        }
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
