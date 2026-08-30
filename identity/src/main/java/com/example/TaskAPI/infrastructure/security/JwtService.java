package com.example.TaskAPI.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HexFormat;

@Service
public final class JwtService {
    public static final String CLAIM_USER_ID = "uid";
    public static final String CLAIM_ROLES = "roles";

    private final SecretKey secretKey;
    private final long expirationsMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationMs) {
        byte[] keyBytes = HexFormat.of().parseHex(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationsMs = expirationMs;
    }

    public String generateToken(UserDetails userDetails) {
        JwtBuilder builder = Jwts.builder()
                .subject(userDetails.getUsername())
                .claim(CLAIM_ROLES, userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationsMs))
                .signWith(secretKey);

        if (userDetails instanceof CustomUserDetails customUserDetails) {
            builder.claim(CLAIM_USER_ID, customUserDetails.getId());
        }

        return builder.compact();
    }

    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (ExpiredJwtException | JwtProcessingException e) {
            return false;
        }
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername());
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    private Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) { // NOPMD
            // intentional pass-through so ExpiredJwtException isn't wrapped by the catch(Exception) below
            throw ex;
        } catch (SignatureException ex) {
            throw new JwtProcessingException("Invalid JWT signature", ex);
        } catch (MalformedJwtException ex) {
            throw new JwtProcessingException("Malformed JWT token", ex);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new JwtProcessingException("Invalid JWT token", ex);
        }
    }
}