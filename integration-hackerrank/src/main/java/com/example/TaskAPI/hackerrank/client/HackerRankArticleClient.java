package com.example.TaskAPI.hackerrank.client;

import com.example.TaskAPI.hackerrank.client.dto.HackerRankArticle;
import com.example.TaskAPI.hackerrank.client.dto.HackerRankPage;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/api/articles")
public interface HackerRankArticleClient {
    @GetExchange
    HackerRankPage<HackerRankArticle> fetchPage(@RequestParam("page") int page);
}
