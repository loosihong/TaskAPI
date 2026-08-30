package com.example.TaskAPI.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

public final class JwtVerifier {
    public static final String CLAIM_USER_ID = "uid";
    public static final String CLAIM_ROLES = "roles";
    public static final String DEFAULT_ROLE = "ROLE_USER";

    private final SecretKey secretKey;

    public JwtVerifier(JwtProperties properties) {
        this.secretKey = Keys.hmacShaKeyFor(HexFormat.of().parseHex(properties.secret()));
    }

    public Optional<JwtPrincipal> verify(String token) {
        Claims claims;

        try {
            claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }

        return Optional.of(toPrincipal(claims));
    }

    private JwtPrincipal toPrincipal(Claims claims) {
        if (!(claims.get(CLAIM_USER_ID) instanceof Number userId)) {
            throw new JwtProcessingException("Token is missing a numeric '" + CLAIM_USER_ID + "' claim", null);
        }

        return new JwtPrincipal(userId.longValue(), claims.getSubject(), authorities(claims));
    }

    private List<SimpleGrantedAuthority> authorities(Claims claims) {
        if (claims.get(CLAIM_ROLES) instanceof Collection<?> roles && !roles.isEmpty()) {
            return roles.stream()
                    .map(String::valueOf)
                    .map(SimpleGrantedAuthority::new)
                    .toList();
        }

        return List.of(new SimpleGrantedAuthority(DEFAULT_ROLE));
    }
}
