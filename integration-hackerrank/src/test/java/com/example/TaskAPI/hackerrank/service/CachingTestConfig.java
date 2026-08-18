package com.example.TaskAPI.hackerrank.service;

import com.example.TaskAPI.hackerrank.client.HackerRankArticleClient;
import org.mockito.Mockito;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableCaching
@Configuration
public class CachingTestConfig {
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("hackerrank-articles");
    }

    @Bean
    public HackerRankArticleClient hackerRankArticleClient() {
        return Mockito.mock(HackerRankArticleClient.class);
    }

    @Bean
    public HackerRankArticleService hackerRankArticleService(HackerRankArticleClient articleClient) {
        return new HackerRankArticleService(articleClient);
    }
}
