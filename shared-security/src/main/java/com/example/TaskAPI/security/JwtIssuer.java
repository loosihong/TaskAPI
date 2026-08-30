package com.example.TaskAPI.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;

public final class JwtIssuer {
    private final SecretKey secretKey;
    private final Duration expiration;

    public JwtIssuer(JwtProperties properties) {
        this.secretKey = Keys.hmacShaKeyFor(HexFormat.of().parseHex(properties.secret()));
        this.expiration = properties.expiration();
    }

    public String issue(Long userId, String username, List<String> roles) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(username)
                .claim(JwtVerifier.CLAIM_USER_ID, userId)
                .claim(JwtVerifier.CLAIM_ROLES, roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiration)))
                .signWith(secretKey)
                .compact();
    }
}
