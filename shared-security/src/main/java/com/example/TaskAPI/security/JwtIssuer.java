package com.example.TaskAPI.security;

import io.jsonwebtoken.Jwts;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;

public final class JwtIssuer {
    private final PrivateKey privateKey;
    private final Duration expiration;
    private final String issuer;
    private final String audience;

    public JwtIssuer(JwtSigningProperties signingProperties, JwtProperties properties) {
        this.privateKey = decodePrivateKey(signingProperties.privateKey());
        this.expiration = signingProperties.expiration();
        this.issuer = properties.issuer();
        this.audience = properties.audience();
    }

    private static PrivateKey decodePrivateKey(String base64) {
        try {
            byte[] der = Base64.getDecoder().decode(base64);
            KeyFactory keyFactory = KeyFactory.getInstance("Ed25519");

            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (GeneralSecurityException | IllegalArgumentException ex) {
            throw new JwtProcessingException("Invalid JWT rpivate key configuration", ex);
        }
    }

    public String issue(Long userId, String username, List<String> roles) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(username)
                .issuer(issuer)
                .audience().add(audience).and()
                .claim(JwtVerifier.CLAIM_USER_ID, userId)
                .claim(JwtVerifier.CLAIM_ROLES, roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiration)))
                .signWith(privateKey, Jwts.SIG.EdDSA)
                .compact();
    }
}
