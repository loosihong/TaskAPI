package com.example.TaskAPI.core.audit;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

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
