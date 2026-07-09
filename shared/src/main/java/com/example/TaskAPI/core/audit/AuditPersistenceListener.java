package com.example.TaskAPI.core.audit;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
public class AuditPersistenceListener {
    private final AuditLogRepository auditLogRepository;

    public AuditPersistenceListener(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Async("auditExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAuditEntryRecorded(AuditEntry auditEntry) {
        AuditLog auditLog = AuditLog.builder()
                .entityName(auditEntry.entityName())
                .entityUuid(auditEntry.entityUUID())
                .build();
        List<AuditFieldLog> fieldLogs = auditEntry.fieldDiffs().stream()
                .map(fieldDiff -> AuditFieldLog.builder()
                        .fieldName(fieldDiff.fieldName())
                        .oldValue(fieldDiff.oldValue())
                        .newValue(fieldDiff.newValue())
                        .auditLog(auditLog)
                        .build())
                .toList();

        auditLog.setAuditFieldLogs(fieldLogs);
        auditLogRepository.save(auditLog);
    }
}
