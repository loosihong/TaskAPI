package com.example.TaskAPI.core.exception;

import java.util.UUID;

public class EntityNotFoundException extends RuntimeException {
    public <T> EntityNotFoundException(Class<T> classType, UUID uuid) {
        super(classType.getSimpleName() + " not found with uuid: " + uuid);
    }
}
