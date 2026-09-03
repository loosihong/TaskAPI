package com.example.TaskAPI.auth.service;

import com.example.TaskAPI.auth.api.AuthResponse;
import com.example.TaskAPI.user.domain.entity.User;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    User register(String username, String password);

    AuthResponse login(String username, String password);
}
