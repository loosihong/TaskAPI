package com.example.TaskAPI.infrastructure.config;

import com.example.TaskAPI.security.JwtPrincipal;
import com.example.TaskAPI.security.JwtVerifier;
import com.example.TaskAPI.task.api.TaskController;
import com.example.TaskAPI.task.mapper.TaskCommentMapperImpl;
import com.example.TaskAPI.task.mapper.TaskMapperImpl;
import com.example.TaskAPI.task.service.TaskCommentService;
import com.example.TaskAPI.task.service.TaskService;
import com.example.TaskAPI.web.BaseControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@Import({
        TaskMapperImpl.class,
        TaskCommentMapperImpl.class,
        TaskSecurityConfig.class})
public class TaskSecurityTest extends BaseControllerTest {
    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private TaskCommentService taskCommentService;

    @Test
    void getTasks_withNoToken_returns401() throws Exception {
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTasks_withToken_returns200() throws Exception {
        when(jwtVerifier.verify(anyString()))
                .thenReturn(Optional.of(new JwtPrincipal(
                        1L, "sihong", List.of(new SimpleGrantedAuthority(JwtVerifier.DEFAULT_ROLE)))));
        when(taskService.getAllTasks())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/tasks")
                        .header("Authorization", "Bearer fake.token.value"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void getTasks_withInvalidToken_returns401() throws Exception {
        when(jwtVerifier.verify(anyString()))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/tasks")
                        .header("Authorization", "Bearer fake.token.value"))
                .andExpect(status().isUnauthorized());
    }
}
