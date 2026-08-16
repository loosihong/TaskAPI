package com.example.TaskAPI.hackerrank.service;

import com.example.TaskAPI.hackerrank.client.HackerRankArticleClient;
import com.example.TaskAPI.hackerrank.client.dto.HackerRankArticle;
import com.example.TaskAPI.hackerrank.client.dto.HackerRankPage;
import com.example.TaskAPI.hackerrank.exception.HackerRankApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HackerRankArticleService {
    private static final int MAX_PAGES = 20;

    private final HackerRankArticleClient articleClient;

    public HackerRankPage<HackerRankArticle> fetchPage(int page) {
        try {
            return articleClient.fetchPage(page);
        } catch (RestClientResponseException ex) {
            throw new HackerRankApiException(
                    "HackerRank returned " + ex.getStatusCode() + "for page " + page, ex);
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
