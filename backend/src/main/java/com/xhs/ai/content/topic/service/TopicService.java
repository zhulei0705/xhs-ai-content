package com.xhs.ai.content.topic.service;

import com.xhs.ai.content.common.api.PageResult;
import com.xhs.ai.content.topic.dto.TopicCreateRequest;
import com.xhs.ai.content.topic.dto.TopicGenerateRequest;
import com.xhs.ai.content.topic.dto.TopicPageRequest;
import com.xhs.ai.content.topic.dto.TopicStatusUpdateRequest;
import com.xhs.ai.content.topic.dto.TopicUpdateRequest;
import com.xhs.ai.content.topic.vo.TopicVO;
import java.time.LocalDate;
import java.util.List;

public interface TopicService {

    List<TopicVO> generate(TopicGenerateRequest request);

    PageResult<TopicVO> page(TopicPageRequest request);

    TopicVO detail(Long id);

    TopicVO create(TopicCreateRequest request);

    TopicVO update(TopicUpdateRequest request);

    void delete(Long id);

    void updateStatus(TopicStatusUpdateRequest request);

    boolean existsGeneratedOn(LocalDate date);
}
