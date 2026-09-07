package com.example.TaskAPI.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("jwt")
public record JwtProperties(
        String publicKey,
        String issuer,
        String audience
) {
}