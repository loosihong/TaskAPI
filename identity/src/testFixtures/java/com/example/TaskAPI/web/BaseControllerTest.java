package com.example.TaskAPI.web;

import com.example.TaskAPI.core.exception.GlobalExceptionHandler;
import com.example.TaskAPI.infrastructure.security.CustomUserDetailsService;
import com.example.TaskAPI.infrastructure.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
public abstract class BaseControllerTest {
    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected JwtService jwtService;

    @MockitoBean
    protected CustomUserDetailsService customUserDetailsService;

    @Autowired
    protected ObjectMapper objectMapper;
}
