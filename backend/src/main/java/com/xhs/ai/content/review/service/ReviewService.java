package com.xhs.ai.content.review.service;

import com.xhs.ai.content.review.dto.ReviewConfirmRequest;
import com.xhs.ai.content.review.dto.ReviewExecuteRequest;
import com.xhs.ai.content.review.vo.AiReviewVO;
import java.util.List;

public interface ReviewService {

    AiReviewVO execute(ReviewExecuteRequest request);

    List<AiReviewVO> history(Long contentId);

    void confirm(ReviewConfirmRequest request);
}
