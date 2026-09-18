package com.example.TaskAPI.core.dto;

import com.example.TaskAPI.core.validation.ValidEntityRequest;

import java.util.UUID;

@ValidEntityRequest
public interface BaseEntityDetailRequest {
    UUID uuid();

    Integer version();
}
