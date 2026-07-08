package com.example.TaskAPI.core.audit;

import com.example.TaskAPI.core.model.BaseRecord;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AuditLog extends BaseRecord {
    @Size(max = 63)
    @Column(nullable = false)
    private String entityName;

    @Column(nullable = false)
    private UUID entityUuid;

    @Singular
    @Setter
    @OneToMany(mappedBy = "auditLog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuditFieldLog> auditFieldLogs;
}
