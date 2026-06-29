package com.example.TaskAPI.task.domain.entity;

import com.example.TaskAPI.core.audit.Auditable;
import com.example.TaskAPI.core.audit.annotation.AuditableField;
import com.example.TaskAPI.core.model.BaseEntity;
import com.example.TaskAPI.core.validation.ValidationError;
import com.example.TaskAPI.user.domain.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldNameConstants
@Entity
public class Task extends BaseEntity implements Auditable {
    @Size(max = Constraints.Values.TITLE_MAX)
    @Column(nullable = false)
    private String title;

    @AuditableField
    @Size(max = Constraints.Values.STATUS_MAX)
    @Column(nullable = false)
    private String status;

    @OneToOne(
            mappedBy = Reference.TABLE_NAME,
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private TaskDetail taskDetail;

    @Builder.Default
    @OneToMany(
            mappedBy = Reference.TABLE_NAME,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<TaskComment> taskComments = new ArrayList<>();

    @Builder.Default
    @OneToMany(
            mappedBy = Reference.TABLE_NAME,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<TaskAssignee> taskAssignees = new HashSet<>();

    public void setTaskDetail(TaskDetail taskDetail) {
        if (this.taskDetail == taskDetail) {
            taskDetail.setTask(this);
            return;
        }

        this.taskDetail = taskDetail;

        if (taskDetail != null
                && taskDetail.getTask() == null) {
            taskDetail.setTask(this);
        }
    }

    public void addComment(TaskComment taskComment) {
        this.taskComments.add(taskComment);
        taskComment.setTask(this);
    }

    public void removeComment(TaskComment taskComment) {
        this.taskComments.remove(taskComment);
        taskComment.setTask(null);
    }

    public void addAssignee(User user) {
        TaskAssignee taskAssignee = TaskAssignee.builder()
                .task(this)
                .user(user)
                .build();
        this.taskAssignees.add(taskAssignee);
    }

    public void syncAssignees(Set<User> users) {
        Set<UUID> userUuids = users.stream()
                .map(user -> user.getUuid())
                .collect(Collectors.toSet());

        this.taskAssignees.removeIf(taskAssignee ->
                !userUuids.contains(taskAssignee.getUser().getUuid()));

        Set<UUID> existingUserUuids = this.taskAssignees.stream()
                .map(taskAssignee -> taskAssignee.getUser().getUuid())
                .collect(Collectors.toSet());

        users.stream()
                .filter(user -> !existingUserUuids.contains(user.getUuid()))
                .forEach(user -> addAssignee(user));
    }

    public static final class Reference {
        public static final String TABLE_NAME = "task";
        public static final String IDENTIFIER_NAME = "task_id";

        private Reference() {
        }
    }

    public static final class Constraints {
        private Constraints() {
        }

        public static final class Values {
            public static final int TITLE_MAX = 255;
            public static final int STATUS_MAX = 31;

            private Values() {
            }
        }

        public static final class Messages {
            public static final String TITLE_REQUIRED = "Title" + ValidationError.REQUIRED;
            public static final String TITLE_MAX = "Title" + ValidationError.MAX_LENGTH;
            public static final String STATUS_REQUIRED = "Status" + ValidationError.REQUIRED;
            public static final String STATUS_MAX = "Status" + ValidationError.MAX_LENGTH;

            private Messages() {
            }
        }
    }
}