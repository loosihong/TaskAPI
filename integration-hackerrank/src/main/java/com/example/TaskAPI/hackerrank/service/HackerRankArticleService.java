package com.example.TaskAPI.hackerrank.service;

import com.example.TaskAPI.hackerrank.client.HackerRankArticleClient;
import com.example.TaskAPI.hackerrank.client.dto.HackerRankArticle;
import com.example.TaskAPI.hackerrank.client.dto.HackerRankPage;
import com.example.TaskAPI.hackerrank.exception.HackerRankApiException;
import com.example.TaskAPI.hackerrank.exception.HackerRankUnavailableException;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class HackerRankArticleService {
    private static final int MAX_PAGES = 20;
    private static final int MAX_RETIRES = 2;

    private final HackerRankArticleClient articleClient;
    private final RetryTemplate retryTemplate;

    public HackerRankArticleService(HackerRankArticleClient articleClient) {
        this.articleClient = articleClient;
        this.retryTemplate = new RetryTemplate(RetryPolicy.builder()
                .includes(HackerRankUnavailableException.class, ResourceAccessException.class)
                .maxRetries(MAX_RETIRES)
                .delay(Duration.ofMillis(200))
                .jitter(Duration.ofMillis(50))
                .multiplier(2)
                .maxDelay(Duration.ofSeconds(2))
                .build());
    }

    public HackerRankPage<HackerRankArticle> fetchPage(int page) {
        try {
            return retryTemplate.invoke(() -> articleClient.fetchPage(page));
        } catch (ResourceAccessException ex) {
            throw new HackerRankApiException("HackerRank is unreachable", ex);
        }
    }

    public List<HackerRankArticle> fetchAllTitledArticles() {
        List<HackerRankPage<HackerRankArticle>> pages = new ArrayList<>();
        int pageNum = 1;
        int totalPages;

        do {
            HackerRankPage<HackerRankArticle> page = fetchPage(pageNum);
            pages.add(page);
            totalPages = page.totalPages();
            pageNum++;
        } while (pageNum <= totalPages && pageNum <= MAX_PAGES);

        return pages.stream()
                .flatMap(page -> page.data().stream())
                .filter(article -> article.displayTitle() != null)
                .toList();
    }
}
