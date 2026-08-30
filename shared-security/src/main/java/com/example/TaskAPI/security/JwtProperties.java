package com.example.TaskAPI.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("jwt")
public record JwtProperties(
        String secret,
        Duration expiration
) {
    public JwtProperties {
        expiration = expiration != null ? expiration : Duration.ofHours(1);
    }
}
