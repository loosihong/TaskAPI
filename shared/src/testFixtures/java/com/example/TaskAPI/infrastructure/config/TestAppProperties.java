package com.example.TaskAPI.infrastructure.config;

import java.time.ZoneId;

public final class TestAppProperties {
    public static final ZoneId TEST_ZONE = ZoneId.of("Asia/Singapore");

    private TestAppProperties() {
    }

    public static AppProperties defaults() {
        return new AppProperties(TEST_ZONE);
    }
}
