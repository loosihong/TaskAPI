package com.example.TaskAPI.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class JwtVerifier {
    public static final String CLAIM_USER_ID = "uid";
    public static final String CLAIM_ROLES = "roles";
    public static final String DEFAULT_ROLE = "ROLE_USER";

    private final PublicKey publicKey;
    private final String issuer;
    private final String audience;

    public JwtVerifier(JwtProperties properties) {
        this.publicKey = decodePublicKey(properties.publicKey());
        this.issuer = properties.issuer();
        this.audience = properties.audience();
    }

    public static PublicKey decodePublicKey(String base64) {
        try {
            byte[] der = Base64.getDecoder().decode(base64);
            KeyFactory keyFactory = KeyFactory.getInstance("Ed25519");

            return keyFactory.generatePublic(new X509EncodedKeySpec(der));
        } catch (GeneralSecurityException | IllegalArgumentException ex) {
            throw new JwtProcessingException("Invalid JWT public key configuration", ex);
        }
    }

    public Optional<JwtPrincipal> verify(String token) {
        Claims claims;

        try {
            claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .requireIssuer(issuer)
                    .requireAudience(audience)
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
