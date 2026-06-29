package com.example.TaskAPI.core.dto;

import java.util.UUID;

public interface BaseEntityDetailResponse {
    UUID uuid();

    Integer version();
}
