package com.xhs.ai.content.publish.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import org.hibernate.validator.constraints.URL;

public record PublishRecordSaveRequest(
        @NotNull Long contentId,
        @NotNull LocalDateTime publishedAt,
        @NotBlank @Size(max = 500) @URL(message = "笔记链接格式不正确") String noteUrl,
        @NotNull @Min(0) Long exposureCount,
        @NotNull @Min(0) Long likeCount,
        @NotNull @Min(0) Long favoriteCount,
        @NotNull @Min(0) Long commentCount,
        @NotNull Integer followerGrowth) {
}
