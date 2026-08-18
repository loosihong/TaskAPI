package com.example.TaskAPI.hackerrank.service;

import com.example.TaskAPI.hackerrank.client.HackerRankArticleClient;
import com.example.TaskAPI.hackerrank.client.dto.HackerRankArticle;
import com.example.TaskAPI.hackerrank.client.dto.HackerRankPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CachingTestConfig.class)
public class HackerRankArticleServiceCachingTest {
    @Autowired
    private HackerRankArticleService articleService;
    @Autowired
    private HackerRankArticleClient articleClient;
    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void resetState() {
        Mockito.reset(articleClient);

        Cache cache = cacheManager.getCache("hackerrank-articles");

        if (cache != null) {
            cache.clear();
        }
    }

    private HackerRankPage<HackerRankArticle> pageOf(int page) {
        return new HackerRankPage<>(page, 10, 20, 2, List.of());
    }

    @Test
    @DisplayName("repeated calls for the same page hit upstream once")
    void fetchPage_samePageTwice_callsClientOnce() {
        when(articleClient.fetchPage(1)).thenReturn(pageOf(1));

        articleService.fetchPage(1);
        articleService.fetchPage(1);

        verify(articleClient, times(1)).fetchPage(1);
    }

    @Test
    @DisplayName("page number is part of the cache key")
    void fetchPage_differentPages_callsClientPerPage() {
        when(articleClient.fetchPage(1)).thenReturn(pageOf(1));
        when(articleClient.fetchPage(2)).thenReturn(pageOf(2));

        assertThat(articleService.fetchPage(1).page()).isEqualTo(1);
        assertThat(articleService.fetchPage(2).page()).isEqualTo(2);

        verify(articleClient, times(1)).fetchPage(1);
        verify(articleClient, times(1)).fetchPage(2);
    }
}
