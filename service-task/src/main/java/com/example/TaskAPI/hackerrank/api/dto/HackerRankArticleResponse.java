package com.example.TaskAPI.hackerrank.api.dto;

import com.example.TaskAPI.hackerrank.client.dto.HackerRankArticle;

public record HackerRankArticleResponse(
        String title,
        String url,
        String author,
        Integer commentCount
) {
    public static HackerRankArticleResponse from(HackerRankArticle article) {
        return new HackerRankArticleResponse(
                article.displayTitle(),
                article.url() != null ? article.url() : article.storyUrl(),
                article.author(),
                article.numComments());
    }
}
