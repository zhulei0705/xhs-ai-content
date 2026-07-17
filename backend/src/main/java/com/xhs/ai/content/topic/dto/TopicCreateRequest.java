package com.xhs.ai.content.topic.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record TopicCreateRequest(
        @NotBlank @Size(max = 200) String topicTitle,
        @NotBlank @Size(max = 500) String coreAngle,
        @NotBlank @Size(max = 200) String targetAudience,
        @NotBlank @Size(max = 32) String contentType,
        @NotEmpty @Size(max = 10) List<@NotBlank @Size(max = 30) String> keywords,
        @NotNull @Min(0) @Max(100) Integer targetMatchScore,
        @NotNull @Min(0) @Max(100) Integer viralPotentialScore,
        @NotNull @Min(0) @Max(100) Integer overallScore,
        @NotBlank @Size(max = 1000) String potentialAnalysis,
        @NotNull @Min(1) @Max(10) Integer publishPriority,
        LocalDate generationDate) {
}
