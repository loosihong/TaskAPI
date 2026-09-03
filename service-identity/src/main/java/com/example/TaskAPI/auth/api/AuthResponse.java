package com.example.TaskAPI.auth.api;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class AuthResponse {
    String token;
}