package com.example.TaskAPI.core.audit;

import com.example.TaskAPI.core.audit.annotation.AuditableField;
import com.example.TaskAPI.core.model.BaseEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PreUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class ReflectionAuditListener {
    private static EntityManager entityManager;
    private static AuditLogRepository auditLogRepository;

    @Autowired
    public void init(EntityManager entityManager, AuditLogRepository auditLogRepository) {
        ReflectionAuditListener.entityManager = entityManager;
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
            if (field.isAnnotationPresent(AuditableField.class)) {
                try {
                    field.setAccessible(true);
                    Object oldValue = baseEntity.getSnapshot().get(field.getName());
                    Object newValue = field.get(entity);

                    if (!Objects.equals(oldValue, newValue)) {
                        AuditableField annotation = field.getAnnotation(AuditableField.class);
                        String fieldLabel = (annotation.displayName().isEmpty() ?
                                field.getName() : annotation.displayName());
                        auditFieldLogs.add(AuditFieldLog.builder()
                                .fieldName(fieldLabel)
                                .oldValue(String.valueOf(oldValue))
                                .newValue(String.valueOf(newValue))
                                .auditLog(auditLog)
                                .build());
                    }
                } catch (IllegalAccessException ex) {
                    //TODO: Log error
                }
            }
        }

        if (!auditFieldLogs.isEmpty()) {
            auditLog.setAuditFieldLogs(auditFieldLogs);
            auditLogRepository.save(auditLog);
        }
    }
}
