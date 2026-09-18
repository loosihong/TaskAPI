package com.example.TaskAPI.core.dto;

import com.example.TaskAPI.core.validation.ValidEntityRequest;

@ValidEntityRequest
public interface BaseExtensionEntityDetailRequest {
    Integer version();
}
