package com.example.TaskAPI.core.audit;

import java.util.List;
import java.util.UUID;

public record AuditEntry(
        String entityName,
        UUID entityUUID,
        Long auditorId,
        List<FieldDiff> fieldDiffs
) {
    public AuditEntry {
        fieldDiffs = List.copyOf(fieldDiffs);
    }

    public record FieldDiff(
            String fieldName,
            String oldValue,
            String newValue
    ) {}
}
