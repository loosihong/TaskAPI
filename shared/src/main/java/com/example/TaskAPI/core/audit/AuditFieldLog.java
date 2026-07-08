package com.example.TaskAPI.core.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditFieldLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Size(max = 63)
    @Column(nullable = false)
    private String fieldName;
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String oldValue;
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String newValue;
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audit_log_id")
    private AuditLog auditLog;
}
