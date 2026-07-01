package com.example.TaskAPI.core.dto;

import java.util.UUID;

public interface BaseEntityDetailRequest {
    UUID uuid();

    Integer version();
}
