package com.example.TaskAPI.core.audit;

import com.example.TaskAPI.core.audit.annotation.AuditableField;
import com.example.TaskAPI.core.model.BaseEntity;
import jakarta.persistence.PreUpdate;
import org.apache.logging.log4j.internal.annotation.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class ReflectionAuditListener {
    private static final Logger log = LoggerFactory.getLogger(ReflectionAuditListener.class);
    private static AuditLogRepository auditLogRepository;

    @SuppressFBWarnings(
            value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "Bridges Spring DI into Hibernate-instantiated @EntityListeners instance, "
                    + "which bypasses Spring's IoC container entirely"
    )
    @Autowired
    public void init(AuditLogRepository auditLogRepository) {
        ReflectionAuditListener.auditLogRepository = auditLogRepository;
    }

    @PreUpdate
    public void onPreUpdate(Object entity) {
        if (!((entity instanceof BaseEntity baseEntity)
                && (entity instanceof Auditable auditable))) {
            return;
        }

        AuditLog auditLog = AuditLog.builder()
                .entityName(entity.getClass().getSimpleName())
                .entityUuid(auditable.getUuid())
                .build();
        List<AuditFieldLog> auditFieldLogs = new ArrayList<>();

        for (Field field : entity.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(AuditableField.class)) {
                continue;
            }

            try {
                field.setAccessible(true);
                Object oldValue = baseEntity.getSnapshot().get(field.getName());
                Object newValue = field.get(entity);

                if (Objects.equals(oldValue, newValue)) {
                    continue;
                }

                AuditableField annotation = field.getAnnotation(AuditableField.class);
                String fieldLabel = annotation.displayName().isEmpty() ?
                        field.getName() : annotation.displayName();
                auditFieldLogs.add(AuditFieldLog.builder()
                        .fieldName(fieldLabel)
                        .oldValue(String.valueOf(oldValue))
                        .newValue(String.valueOf(newValue))
                        .auditLog(auditLog)
                        .build());
            } catch (IllegalAccessException ex) {
                log.warn("Failed to read auditable field '{}' on entity '{}' for audit logging",
                        field.getName(), entity.getClass().getSimpleName(), ex);
            }
        }

        if (!auditFieldLogs.isEmpty()) {
            auditLog.setAuditFieldLogs(auditFieldLogs);
            auditLogRepository.save(auditLog);
        }
    }
}
