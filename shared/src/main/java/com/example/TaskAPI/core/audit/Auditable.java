package com.example.TaskAPI.core.audit;

import java.util.UUID;

public interface Auditable extends Snapshotable {
    UUID getUuid();
}
