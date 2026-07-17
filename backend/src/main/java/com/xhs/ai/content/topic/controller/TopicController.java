package com.xhs.ai.content.topic.controller;

import com.xhs.ai.content.common.api.PageResult;
import com.xhs.ai.content.common.api.Result;
import com.xhs.ai.content.common.dto.IdRequest;
import com.xhs.ai.content.topic.dto.TopicCreateRequest;
import com.xhs.ai.content.topic.dto.TopicGenerateRequest;
import com.xhs.ai.content.topic.dto.TopicPageRequest;
import com.xhs.ai.content.topic.dto.TopicStatusUpdateRequest;
import com.xhs.ai.content.topic.dto.TopicUpdateRequest;
import com.xhs.ai.content.topic.service.TopicService;
import com.xhs.ai.content.topic.vo.TopicVO;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/topics")
public class TopicController {

    private final TopicService topicService;

    @PostMapping("/generate")
    public Result<List<TopicVO>> generate(@Valid @RequestBody TopicGenerateRequest request) {
        return Result.success(topicService.generate(request));
    }

    @PostMapping("/page")
    public Result<PageResult<TopicVO>> page(@Valid @RequestBody TopicPageRequest request) {
        return Result.success(topicService.page(request));
    }

    @PostMapping("/detail")
    public Result<TopicVO> detail(@Valid @RequestBody IdRequest request) {
        return Result.success(topicService.detail(request.id()));
    }

    @PostMapping("/create")
    public Result<TopicVO> create(@Valid @RequestBody TopicCreateRequest request) {
        return Result.success(topicService.create(request));
    }

    @PostMapping("/update")
    public Result<TopicVO> update(@Valid @RequestBody TopicUpdateRequest request) {
        return Result.success(topicService.update(request));
    }

    @PostMapping("/delete")
    public Result<Void> delete(@Valid @RequestBody IdRequest request) {
        topicService.delete(request.id());
        return Result.success();
    }

    @PostMapping("/status/update")
    public Result<Void> updateStatus(@Valid @RequestBody TopicStatusUpdateRequest request) {
        topicService.updateStatus(request);
        return Result.success();
    }
}
