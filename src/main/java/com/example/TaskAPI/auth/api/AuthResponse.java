package com.example.TaskAPI.auth.api;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class AuthResponse {
    private String token;
}