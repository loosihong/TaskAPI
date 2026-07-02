package com.example.TaskAPI.task.domain.entity;

import com.example.TaskAPI.core.model.BaseEntity;
import com.example.TaskAPI.core.validation.ValidationError;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldNameConstants
@Entity
@Table(
        indexes = @Index(name = "idx_task_id", columnList =
                Task.Reference.IDENTIFIER_NAME)
)
public class TaskComment extends BaseEntity {
    @Column(name = Task.Reference.IDENTIFIER_NAME, insertable = false, updatable = false)
    private Long taskId;
    @Size(max = Constraints.Values.COMMENT_MAX)
    private String comment;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = Task.Reference.IDENTIFIER_NAME, nullable = false)
    private Task task;

    public static final class Constraints {
        private Constraints() {
        }

        public static final class Values {
            public static final int COMMENT_MAX = 511;

            private Values() {
            }
        }

        public static final class Messages {
            public static final String COMMENT_MAX = "Comment" + ValidationError.MAX_LENGTH;

            private Messages() {
            }
        }
    }
}
