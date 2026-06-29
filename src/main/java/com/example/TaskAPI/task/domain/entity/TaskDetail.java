package com.example.TaskAPI.task.domain.entity;

import com.example.TaskAPI.core.model.BaseExtensionEntity;
import com.example.TaskAPI.core.validation.ValidationError;
import com.example.TaskAPI.task.domain.enums.Priority;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldNameConstants
@Entity
public class TaskDetail extends BaseExtensionEntity {
    @Setter(AccessLevel.NONE)
    @OneToOne(fetch = FetchType.EAGER)
    @MapsId
    @JoinColumn(name = "id", nullable = false, unique = true)
    private Task task;
    @Size(max = Constraints.Values.DESCRIPTION_MAX)
    private String description;
    @Future
    private LocalDate dueDate;
    @Convert(converter = Priority.Converter.class)
    @Column(nullable = false)
    private Priority priority;

    public void setTask(Task task) {
        if (this.task == task) {
            task.setTaskDetail(this);
            return;
        }

        this.task = task;

        if (task != null
                && task.getTaskDetail() == null) {
            task.setTaskDetail(this);
        }
    }

    public static final class Constraints {
        private Constraints() {
        }

        public static final class Values {
            public static final int DESCRIPTION_MAX = 1023;

            public Values() {
            }
        }

        public static final class Messages {
            public static final String DESCRIPTION_MAX = "Description" + ValidationError.MAX_LENGTH;
            public static final String DUE_DATE_FUTURE = "Due Date" + ValidationError.FUTURE;
            public static final String PRIORITY_REQUIRED = "Priority" + ValidationError.REQUIRED;

            public Messages() {
            }
        }
    }
}
