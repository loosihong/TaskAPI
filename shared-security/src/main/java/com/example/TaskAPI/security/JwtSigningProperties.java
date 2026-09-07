package com.example.TaskAPI.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("jwt.signing")
public record JwtSigningProperties(
        String privateKey,
        Duration expiration
) {
    public JwtSigningProperties {
        expiration = expiration != null ? expiration : Duration.ofHours(1);
    }
}
