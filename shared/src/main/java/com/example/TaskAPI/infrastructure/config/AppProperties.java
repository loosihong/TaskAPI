package com.example.TaskAPI.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.ZoneId;

@ConfigurationProperties("app")
public record AppProperties(ZoneId timezone) {
    public AppProperties {
        timezone = timezone != null ? timezone : ZoneId.of("Asia/Singapore");
    }
}
