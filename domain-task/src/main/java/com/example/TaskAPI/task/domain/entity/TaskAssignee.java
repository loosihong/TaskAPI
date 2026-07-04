package com.example.TaskAPI.task.domain.entity;

import com.example.TaskAPI.core.model.BaseRecord;
import com.example.TaskAPI.user.domain.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldNameConstants
@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {
                Task.Reference.IDENTIFIER_NAME,
                User.Reference.IDENTIFIER_NAME}),
        indexes = {
                @Index(name = "idx_task_id", columnList =
                        Task.Reference.IDENTIFIER_NAME + "," + User.Reference.IDENTIFIER_NAME),
                @Index(name = "idx_user_id", columnList =
                        User.Reference.IDENTIFIER_NAME + "," + Task.Reference.IDENTIFIER_NAME)
        }
)
public class TaskAssignee extends BaseRecord {
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = Task.Reference.IDENTIFIER_NAME, nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = User.Reference.IDENTIFIER_NAME, nullable = false)
    private User user;
}
