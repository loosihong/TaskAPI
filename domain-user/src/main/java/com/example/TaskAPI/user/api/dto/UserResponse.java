package com.example.TaskAPI.user.api.dto;

import lombok.Builder;

import java.util.UUID;

public interface UserResponse {
    @Builder
    record Summary(
            UUID uuid,
            String username
    ) {
    }
}
