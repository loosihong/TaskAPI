package com.example.TaskAPI.infrastructure.config;

import com.example.TaskAPI.web.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import static org.assertj.core.api.Assertions.assertThat;

public class CacheProviderTest extends BaseIntegrationTest {
    @Autowired
    private CacheManager cacheManager;

    @Test
    void cacheManager_withRedisOnClasspath_isStillCaffine() {
        assertThat(cacheManager).isInstanceOf(CaffeineCacheManager.class);
    }
}
