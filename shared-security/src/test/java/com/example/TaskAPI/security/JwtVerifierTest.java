package com.example.TaskAPI.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Jwks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JwtVerifierTest {
    private static final String ISSUER = "taskapi-identity-test";
    private static final String AUDIENCE = "taskapi-test";

    private String privateKeyB64;
    private String publicKeyB64;
    private JwtProperties jwtProperties;
    private JwtIssuer jwtIssuer;
    private JwtVerifier jwtVerifier;

    @BeforeEach
    void setup() {
        KeyPair keyPair = Jwks.CRV.Ed25519.keyPair().build();

        privateKeyB64 = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        publicKeyB64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        jwtProperties = new JwtProperties(publicKeyB64, ISSUER, AUDIENCE);
        jwtIssuer = new JwtIssuer(new JwtSigningProperties(privateKeyB64, Duration.ofHours(1)), jwtProperties);
        jwtVerifier = new JwtVerifier(jwtProperties);
    }

    @Test
    void verify_withValidToken_returnsPrincipalWithIdAndRoles() {
        String token = jwtIssuer.issue(42L, "sihong", List.of("ROLE_ADMIN"));
        Optional<JwtPrincipal> result = jwtVerifier.verify(token);

        assertThat(result).isPresent();

        JwtPrincipal principal = result.get();

        assertThat(principal.id()).isEqualTo(42L);
        assertThat(principal.username()).isEqualTo("sihong");
        assertThat(principal.authorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void verify_withExpiredToken_returnsEmpty() {
        JwtIssuer expiredIssuer = new JwtIssuer(
                new JwtSigningProperties(privateKeyB64, Duration.ofHours(-1)), jwtProperties);
        String expiredToken = expiredIssuer.issue(42L, "sihong", List.of(JwtVerifier.DEFAULT_ROLE));

        assertThat(jwtVerifier.verify(expiredToken)).isEmpty();
    }

    @Test
    void verify_withTamperedSingature_returnsEmpty() {
        String token = jwtIssuer.issue(42L, "sihong", List.of(JwtVerifier.DEFAULT_ROLE));
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";

        assertThat(jwtVerifier.verify(tamperedToken)).isEmpty();
    }

    @Test
    void verify_withMissingUidClaim_throwsJwtProcessingException() {
        String tokenNoUid = Jwts.builder()
                .subject("sihong")
                .issuer(ISSUER)
                .audience().add(AUDIENCE).and()
                .claim(JwtVerifier.CLAIM_ROLES, List.of(JwtVerifier.DEFAULT_ROLE))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(decodePrivateKeyForTest(), Jwts.SIG.EdDSA)
                .compact();

        assertThatThrownBy(() -> jwtVerifier.verify(tokenNoUid))
                .isInstanceOf(JwtProcessingException.class)
                .hasMessageContaining(JwtVerifier.CLAIM_USER_ID);
    }

    @Test
    void verify_withMissingRolesClaim_defaultsToRoleUser() {
        String tokenNoRoles = Jwts.builder()
                .subject("sihong")
                .issuer(ISSUER)
                .audience().add(AUDIENCE).and()
                .claim(JwtVerifier.CLAIM_USER_ID, 42L)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(decodePrivateKeyForTest(), Jwts.SIG.EdDSA)
                .compact();
        Optional<JwtPrincipal> result = jwtVerifier.verify(tokenNoRoles);

        assertThat(result).isPresent();
        assertThat(result.get().authorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly(JwtVerifier.DEFAULT_ROLE);
    }

    private PrivateKey decodePrivateKeyForTest() {
        try {
            byte[] der = Base64.getDecoder().decode(privateKeyB64);

            return KeyFactory.getInstance("ED25519").generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
