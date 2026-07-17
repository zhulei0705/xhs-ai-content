package com.xhs.ai.content.review.controller;

import com.xhs.ai.content.common.api.Result;
import com.xhs.ai.content.common.dto.IdRequest;
import com.xhs.ai.content.review.dto.ReviewConfirmRequest;
import com.xhs.ai.content.review.dto.ReviewExecuteRequest;
import com.xhs.ai.content.review.service.ReviewService;
import com.xhs.ai.content.review.vo.AiReviewVO;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/execute")
    public Result<AiReviewVO> execute(@Valid @RequestBody ReviewExecuteRequest request) {
        return Result.success(reviewService.execute(request));
    }

    @PostMapping("/history")
    public Result<List<AiReviewVO>> history(@Valid @RequestBody IdRequest request) {
        return Result.success(reviewService.history(request.id()));
    }

    @PostMapping("/confirm")
    public Result<Void> confirm(@Valid @RequestBody ReviewConfirmRequest request) {
        reviewService.confirm(request);
        return Result.success();
    }
}
