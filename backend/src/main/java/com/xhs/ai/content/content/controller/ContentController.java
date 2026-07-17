package com.xhs.ai.content.content.controller;

import com.xhs.ai.content.common.api.PageResult;
import com.xhs.ai.content.common.api.Result;
import com.xhs.ai.content.common.dto.IdRequest;
import com.xhs.ai.content.content.dto.ContentGenerateRequest;
import com.xhs.ai.content.content.dto.ContentPageRequest;
import com.xhs.ai.content.content.dto.ContentSaveRequest;
import com.xhs.ai.content.content.dto.ContentStatusUpdateRequest;
import com.xhs.ai.content.content.service.ContentService;
import com.xhs.ai.content.content.vo.ContentCopyVO;
import com.xhs.ai.content.content.vo.ContentDetailVO;
import com.xhs.ai.content.content.vo.ContentListVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contents")
public class ContentController {

    private final ContentService contentService;

    @PostMapping("/generate")
    public Result<ContentDetailVO> generate(@Valid @RequestBody ContentGenerateRequest request) {
        return Result.success(contentService.generate(request));
    }

    @PostMapping("/page")
    public Result<PageResult<ContentListVO>> page(@Valid @RequestBody ContentPageRequest request) {
        return Result.success(contentService.page(request));
    }

    @PostMapping("/detail")
    public Result<ContentDetailVO> detail(@Valid @RequestBody IdRequest request) {
        return Result.success(contentService.detail(request.id()));
    }

    @PostMapping("/create")
    public Result<ContentDetailVO> create(@Valid @RequestBody ContentSaveRequest request) {
        return Result.success(contentService.create(request));
    }

    @PostMapping("/update")
    public Result<ContentDetailVO> update(@Valid @RequestBody ContentSaveRequest request) {
        return Result.success(contentService.update(request));
    }

    @PostMapping("/delete")
    public Result<Void> delete(@Valid @RequestBody IdRequest request) {
        contentService.delete(request.id());
        return Result.success();
    }

    @PostMapping("/status/update")
    public Result<Void> updateStatus(@Valid @RequestBody ContentStatusUpdateRequest request) {
        contentService.updateStatus(request);
        return Result.success();
    }

    @PostMapping("/copy")
    public Result<ContentCopyVO> copy(@Valid @RequestBody IdRequest request) {
        return Result.success(contentService.copy(request.id()));
    }
}
