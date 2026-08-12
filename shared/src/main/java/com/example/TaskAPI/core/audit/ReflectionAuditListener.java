package com.example.TaskAPI.core.audit;

import com.example.TaskAPI.core.audit.annotation.AuditableField;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import org.apache.logging.log4j.internal.annotation.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
public class ReflectionAuditListener {
    private static final Logger log = LoggerFactory.getLogger(ReflectionAuditListener.class);
    private static ApplicationEventPublisher eventPublisher;
    private static AuditorAware<Long> auditorAware;

    @SuppressFBWarnings(
            value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "Bridges Spring DI into Hibernate-instantiated @EntityListeners instance, "
                    + "which bypasses Spring's IoC container entirely"
    )
    @Autowired
    public void init(ApplicationEventPublisher eventPublisher, AuditorAware<Long> auditorAware) {

        ReflectionAuditListener.eventPublisher = eventPublisher;
        ReflectionAuditListener.auditorAware = auditorAware;
    }

    @PreUpdate
    public void onPreUpdate(Object entity) {
        if (!(entity instanceof Auditable auditable)) {
            return;
        }

        List<AuditEntry.FieldDiff> fieldDiffs = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        for (Class<?> currentClass = entity.getClass();
             currentClass != null && currentClass != Object.class;
             currentClass = currentClass.getSuperclass()) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (!field.isAnnotationPresent(AuditableField.class)
                        || Modifier.isStatic(field.getModifiers())
                        || !visited.add(field.getName())) {
                    continue;
                }

                try {
                    field.setAccessible(true);
                    Object oldValue = auditable.getSnapshot().get(field.getName());
                    Object newValue = field.get(entity);

                    if (Objects.equals(oldValue, newValue)) {
                        continue;
                    }

                    AuditableField annotation = field.getAnnotation(AuditableField.class);
                    String fieldLabel = annotation.displayName().isEmpty() ?
                            field.getName() : annotation.displayName();
                    fieldDiffs.add(new AuditEntry.FieldDiff(
                            fieldLabel,
                            String.valueOf(oldValue),
                            String.valueOf(newValue)));
                } catch (IllegalAccessException ex) {
                    log.warn("Failed to read auditable field '{}' on entity '{}' for audit logging",
                            field.getName(), entity.getClass().getSimpleName(), ex);
                }
            }
        }

        if (!fieldDiffs.isEmpty()) {
            publish(entity, auditable, fieldDiffs);
        }
    }

    @PreRemove
    public void onPreRemove(Object entity) {
        if (!(entity instanceof Auditable auditable)) {
            return;
        }

        publish(entity,
                auditable,
                List.of(new AuditEntry.FieldDiff("deleted", "false", "true")));
    }

    private void publish(Object entity, Auditable auditable, List<AuditEntry.FieldDiff> fieldDiffs) {
        eventPublisher.publishEvent(new AuditEntry(
                entity.getClass().getSimpleName(),
                auditable.getUuid(),
                auditorAware.getCurrentAuditor().orElse(null),
                fieldDiffs));
    }
}
