package com.example.TaskAPI.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JwtVerifierTest {
    private static final String TEST_SECRET =
            "a6d262dab6b4acc1305c26097e8d0923aecda558f99eef1bca3582c5db7df9067e3b6beefb9c18709235e958eac457cd534a69af33d0588dead39eeb6436c22c";

    private JwtIssuer jwtIssuer;
    private JwtVerifier jwtVerifier;
    private SecretKey secretKey;

    @BeforeEach
    void setup() {
        JwtProperties jwtProperties = new JwtProperties(TEST_SECRET, Duration.ofDays(1));
        jwtIssuer = new JwtIssuer(jwtProperties);
        jwtVerifier = new JwtVerifier(jwtProperties);
        secretKey = Keys.hmacShaKeyFor(HexFormat.of().parseHex(TEST_SECRET));
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
        JwtIssuer expiredIssuer = new JwtIssuer(new JwtProperties(TEST_SECRET, Duration.ofDays(-1)));
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
                .claim(JwtVerifier.CLAIM_ROLES, List.of(JwtVerifier.DEFAULT_ROLE))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(secretKey)
                .compact();

        assertThatThrownBy(() -> jwtVerifier.verify(tokenNoUid))
                .isInstanceOf(JwtProcessingException.class)
                .hasMessageContaining(JwtVerifier.CLAIM_USER_ID);
    }

    @Test
    void verify_withMissingRolesClaim_defaultsToRoleUser() {
        String tokenNoRoles = Jwts.builder()
                .subject("sihong")
                .claim(JwtVerifier.CLAIM_USER_ID, 42L)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(secretKey)
                .compact();
        Optional<JwtPrincipal> result = jwtVerifier.verify(tokenNoRoles);

        assertThat(result).isPresent();
        assertThat(result.get().authorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly(JwtVerifier.DEFAULT_ROLE);
    }
}
