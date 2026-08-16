package com.example.TaskAPI.hackerrank.client.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record HackerRankArticle(
        String title,
        String url,
        String author,
        Integer numComments,
        Long storyId,
        String storyTitle,
        String storyUrl,
        Long parentId,
        Long createdAt
) {
    public String displayTitle() {
        if (title != null && !title.isBlank()) {
            return title;
        }

        if (storyTitle != null && !storyTitle.isBlank()) {
            return storyTitle;
        }

        return null;
    }
}
