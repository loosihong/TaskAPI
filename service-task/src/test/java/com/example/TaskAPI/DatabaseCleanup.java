package com.example.TaskAPI;

import com.example.TaskAPI.core.BaseDatabaseCleanup;
import com.example.TaskAPI.core.audit.AuditFieldLog;
import com.example.TaskAPI.core.audit.AuditLog;
import com.example.TaskAPI.task.domain.entity.Task;
import com.example.TaskAPI.task.domain.entity.TaskAssignee;
import com.example.TaskAPI.task.domain.entity.TaskComment;
import com.example.TaskAPI.task.domain.entity.TaskDetail;
import com.example.TaskAPI.user.domain.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatabaseCleanup extends BaseDatabaseCleanup {
    @Override
    protected List<Class<?>> entityClasses() {
        return List.of(TaskAssignee.class, TaskComment.class, TaskDetail.class, Task.class,
                AuditFieldLog.class, AuditLog.class);
    }

    @Override
    protected String userTableName() {
        return User.Reference.TABLE_NAME;
    }
}