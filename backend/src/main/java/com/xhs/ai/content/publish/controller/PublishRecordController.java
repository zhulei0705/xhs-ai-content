package com.xhs.ai.content.publish.controller;

import com.xhs.ai.content.common.api.PageResult;
import com.xhs.ai.content.common.api.Result;
import com.xhs.ai.content.common.dto.IdRequest;
import com.xhs.ai.content.publish.dto.PublishRecordPageRequest;
import com.xhs.ai.content.publish.dto.PublishRecordSaveRequest;
import com.xhs.ai.content.publish.service.PublishRecordService;
import com.xhs.ai.content.publish.vo.PublishRecordVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/publish-records")
public class PublishRecordController {

    private final PublishRecordService publishRecordService;

    @PostMapping("/save")
    public Result<PublishRecordVO> save(@Valid @RequestBody PublishRecordSaveRequest request) {
        return Result.success(publishRecordService.save(request));
    }

    @PostMapping("/page")
    public Result<PageResult<PublishRecordVO>> page(@Valid @RequestBody PublishRecordPageRequest request) {
        return Result.success(publishRecordService.page(request));
    }

    @PostMapping("/detail")
    public Result<PublishRecordVO> detail(@Valid @RequestBody IdRequest request) {
        return Result.success(publishRecordService.detail(request.id()));
    }
}
