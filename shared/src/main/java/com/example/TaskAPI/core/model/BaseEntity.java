package com.example.TaskAPI.core.model;

import com.example.TaskAPI.core.audit.Auditable;
import com.example.TaskAPI.core.audit.ReflectionAuditListener;
import com.example.TaskAPI.core.audit.annotation.AuditableField;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.type.NumericBooleanConverter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@MappedSuperclass
@FieldNameConstants
@EntityListeners({AuditingEntityListener.class, ReflectionAuditListener.class})
@SoftDelete(columnName = "is_deleted", converter = NumericBooleanConverter.class)
public abstract class BaseEntity {
    @Transient
    private final Map<String, Object> snapshot = new HashMap<>();
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;
    @Column(nullable = false, unique = true, updatable = false)
    protected UUID uuid;
    @CreatedDate
    @Column(nullable = false, updatable = false)
    protected LocalDateTime createdAt;
    @CreatedBy
    @Column(updatable = false)
    protected Long createdBy;
    @LastModifiedDate
    @Column(nullable = false)
    protected LocalDateTime updatedAt;
    @LastModifiedBy
    protected Long updatedBy;
    @Setter
    @Version
    protected Integer version;
    @AuditableField
    @Column(name = "is_deleted", insertable = false, updatable = false)
    protected boolean deleted;

    @PrePersist
    public void prePersist() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }

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
                    log.warn("Failed to read auditable field '{}' on entity '{}' for audit logging",
                            field.getName(), this.getClass().getSimpleName(), ex);
                }
            }
        }
    }
}