package com.example.TaskAPI.core.audit;

import java.util.UUID;

public interface Auditable {
    UUID getUuid();

    default String getEntityName() {
        return this.getClass().getSimpleName();
    }
}
