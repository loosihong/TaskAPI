package com.example.TaskAPI.infrastructure.security;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;


public class JwtServiceTest {
    private JwtService jwtService;
    private UserDetails userDetails;
    private String token;

    @BeforeEach
    void setup() {
        String testSecret = "a6d262dab6b4acc1305c26097e8d0923aecda558f99eef1bca3582c5db7df9067e3b6beefb9c18709235e958eac457cd534a69af33d0588dead39eeb6436c22c";
        long testExpiration = 86400000L;
        jwtService = new JwtService(testSecret, testExpiration);
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
}
