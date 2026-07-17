package com.xhs.ai.content.content.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ContentSaveRequest(
        Long id,
        Long topicId,
        @NotBlank @Size(max = 100) String selectedTitle,
        @NotBlank @Size(max = 10000) String body,
        @NotBlank @Size(max = 50) String coverTitle,
        @NotBlank @Size(max = 80) String coverSubtitle,
        @NotEmpty @Size(min = 1, max = 15) List<@NotBlank @Size(max = 30) String> tags,
        @NotBlank @Size(max = 500) String interactionGuide,
        @NotEmpty @Size(min = 1, max = 5) List<@Valid TitleInput> titles,
        @NotEmpty @Size(min = 1, max = 6) List<@Valid CardInput> cards) {

    public record TitleInput(
            @NotBlank @Size(max = 100) String text,
            @NotNull @Min(1) @Max(5) Integer sortOrder,
            @Min(0) @Max(100) Integer attractionScore) {
    }

    public record CardInput(
            @NotNull @Min(1) @Max(6) Integer cardNo,
            @NotBlank @Size(max = 60) String title,
            @NotBlank @Size(max = 500) String body) {
    }
}
