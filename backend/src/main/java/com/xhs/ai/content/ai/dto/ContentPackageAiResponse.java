package com.xhs.ai.content.ai.dto;

import java.util.List;

public record ContentPackageAiResponse(
        List<TitleItem> titles,
        String body,
        String coverTitle,
        String coverSubtitle,
        List<CardItem> cards,
        List<String> tags,
        String interactionGuide) {

    public record TitleItem(String text, Integer attractionScore) {
    }

    public record CardItem(Integer cardNo, String title, String body) {
    }
}
