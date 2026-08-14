package com.example.TaskAPI.infrastructure.security;

import com.example.TaskAPI.task.api.TaskController;
import com.example.TaskAPI.task.mapper.TaskCommentMapperImpl;
import com.example.TaskAPI.task.mapper.TaskMapperImpl;
import com.example.TaskAPI.task.service.TaskCommentService;
import com.example.TaskAPI.task.service.TaskService;
import com.example.TaskAPI.web.BaseControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@Import({
        TaskMapperImpl.class,
        TaskCommentMapperImpl.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class})
public class TaskSecurityTest extends BaseControllerTest {
    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private TaskCommentService taskCommentService;

    @BeforeEach
    void setUp() {
        UserDetails userDetails = User
                .withUsername("sihong")
                .password("secret123")
                .authorities(Collections.emptyList())
                .build();

        when(jwtService.isTokenValid(anyString())).thenReturn(true);
        when(jwtService.extractUsername(anyString())).thenReturn("sihong");
        when(customUserDetailsService.loadUserByUsername("sihong")).thenReturn(userDetails);
        when(jwtService.validateToken(anyString(), any(UserDetails.class))).thenReturn(true);
    }

    @Test
    void getTasks_withNoToken_returns401() throws Exception {
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTasks_withToken_returns200() throws Exception {
        when(taskService.getAllTasks())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/tasks")
                        .header("Authorization", "Bearer fake.token.value"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void getTasks_withInvalidToken_returns401() throws Exception {
        when(jwtService.isTokenValid(anyString())).thenReturn(false);

        mockMvc.perform(get("/tasks")
                        .header("Authorization", "Bearer fake.token.value"))
                .andExpect(status().isUnauthorized());
    }
}
