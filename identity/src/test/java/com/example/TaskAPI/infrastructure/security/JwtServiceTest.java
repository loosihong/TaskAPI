package com.example.TaskAPI.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class JwtServiceTest {
    private JwtService jwtService;
    private UserDetails userDetails;
    private String token;
    private String testSecret;

    @BeforeEach
    void setup() {
        testSecret = "a6d262dab6b4acc1305c26097e8d0923aecda558f99eef1bca3582c5db7df9067e3b6beefb9c18709235e958eac457cd534a69af33d0588dead39eeb6436c22c";
        jwtService = new JwtService(testSecret, 86400000L);
        userDetails = User.withUsername("sihong")
                .password("secret123")
                .authorities(Collections.emptyList())
                .build();
        token = jwtService.generateToken(userDetails);
    }

    @Test
    void generateToken_tokenContainsCorrectUsername() {
        String extractedUsername = jwtService.extractUsername(token);
        assertThat(extractedUsername).isEqualTo(userDetails.getUsername());
    }

    @Test
    void validateToken_withValidToken_returnsTrue() {
        assertThat(jwtService.validateToken(token, userDetails)).isTrue();
    }

    @Test
    void validateToken_withWrongUser_returnsFalse() {
        UserDetails otherUser = User.withUsername("otheruser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        assertThat(jwtService.validateToken(token, otherUser)).isFalse();
    }

    @Test
    void validateToken_withExpiredToken_throwsException() {
        JwtService shortLivedService = new JwtService(
                "a6d262dab6b4acc1305c26097e8d0923aecda558f99eef1bca3582c5db7df9067e3b6beefb9c18709235e958eac457cd534a69af33d0588dead39eeb6436c22c",
                -1000L);
        String expiredToken = shortLivedService.generateToken(userDetails);
        assertThatThrownBy(() -> shortLivedService.validateToken(expiredToken, userDetails))
                .isInstanceOf(ExpiredJwtException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void validateToken_withTamperedToken_throwsException() {
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";
        assertThatThrownBy(() -> jwtService.validateToken(tamperedToken, userDetails))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void generateToken_withCustomUserDetails_embedsUidAndRolesClaims() {
        com.example.TaskAPI.user.domain.entity.User domainUser = com.example.TaskAPI.user.domain.entity.User.builder()
                .id(42L)
                .username("sihong")
                .password("secret1234")
                .build();
        CustomUserDetails customerUserDetails = new CustomUserDetails(
                com.example.TaskAPI.user.domain.entity.User
                        .builder()
                        .id(42L)
                        .username("sihong")
                        .password("secret1234")
                        .build());
        String customToken = jwtService.generateToken(customerUserDetails);
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(HexFormat.of().parseHex(testSecret)))
                .build()
                .parseSignedClaims(customToken)
                .getPayload();
        Object uidClaim = claims.get(JwtService.CLAIM_USER_ID);

        assertThat(uidClaim).isInstanceOf(Number.class);
        assertThat(((Number) uidClaim).longValue()).isEqualTo(domainUser.getId());
        assertThat(claims.get(JwtService.CLAIM_ROLES))
                .asInstanceOf(InstanceOfAssertFactories.LIST)
                .containsExactly("ROLE_USER");
    }

    @Test
    void generateToken_withPlainUserDetails_omitsUidClaim() {
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(HexFormat.of().parseHex(testSecret)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.get(JwtService.CLAIM_USER_ID)).isNull();
    }
}
