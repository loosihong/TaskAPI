package com.example.TaskAPI.auth.api;

import com.example.TaskAPI.auth.service.AuthService;
import com.example.TaskAPI.core.exception.DuplicateEntityException;
import com.example.TaskAPI.infrastructure.security.SecurityConfig;
import com.example.TaskAPI.web.BaseControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
public class AuthControllerTest extends BaseControllerTest {
    @MockitoBean
    private AuthService authService;

    private AuthRequest.Login authRequest;

    @BeforeEach
    void setUp() {
        authRequest = AuthRequest.Login.builder()
                .username("sihong")
                .password("secret123")
                .build();
    }

    @Test
    void register_withDuplicateUsername_returns500() throws Exception {
        doThrow(new DuplicateEntityException("Username already taken"))
                .when(authService).register(any(String.class), any(String.class));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void login_withValidCredentials_returnsToken() throws Exception {
        when(authService.login(any(String.class), any(String.class)))
                .thenReturn(AuthResponse.builder().token("mocked.jwt.token").build());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked.jwt.token"));
    }

    @Test
    void login_withInvalidCredentials_returns401() throws Exception {
        authRequest = AuthRequest.Login.builder()
                .username("sihong")
                .password("wrongsecret")
                .build();

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authService)
                .login(any(String.class), any(String.class));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized());
    }
}