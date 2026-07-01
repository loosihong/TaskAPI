package com.example.TaskAPI.core.model;

import com.example.TaskAPI.core.audit.Auditable;
import com.example.TaskAPI.core.audit.ReflectionAuditListener;
import com.example.TaskAPI.core.audit.annotation.AuditableField;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@MappedSuperclass
@FieldNameConstants
@EntityListeners({AuditingEntityListener.class, ReflectionAuditListener.class})
public abstract class BaseExtensionEntity {
    @Transient
    private final Map<String, Object> snapshot = new HashMap<>();
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;
    @LastModifiedDate
    @Column(nullable = false)
    protected LocalDateTime updatedAt;
    @LastModifiedBy
    protected Long updatedBy;
    @Setter
    @Version
    protected Integer version;

    @PostLoad
    protected void autoSnapshot() {
        if (!(this instanceof Auditable)) {
            return;
        }

        snapshot.clear();

        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(AuditableField.class)) {
                try {
                    field.setAccessible(true);
                    snapshot.put(field.getName(), field.get(this));
                } catch (IllegalAccessException ex) {
                    //TODO: Log error
                }
            }
        }
    }
}