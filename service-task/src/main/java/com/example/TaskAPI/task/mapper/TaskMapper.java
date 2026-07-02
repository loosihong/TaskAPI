package com.example.TaskAPI.task.mapper;

import com.example.TaskAPI.core.mapper.BaseMapperConfig;
import com.example.TaskAPI.core.mapper.annotation.IgnoreBaseEntityMapping;
import com.example.TaskAPI.core.mapper.annotation.IgnoreBaseExtensionEntityMapping;
import com.example.TaskAPI.task.api.dto.TaskDetailRequest;
import com.example.TaskAPI.task.api.dto.TaskDetailResponse;
import com.example.TaskAPI.task.api.dto.TaskRequest;
import com.example.TaskAPI.task.api.dto.TaskResponse;
import com.example.TaskAPI.task.domain.entity.Task;
import com.example.TaskAPI.task.domain.entity.TaskAssignee;
import com.example.TaskAPI.task.domain.entity.TaskDetail;
import com.example.TaskAPI.task.mapper.annotation.IgnoreTaskDetailMapping;
import com.example.TaskAPI.task.mapper.annotation.IgnoreTaskMapping;
import com.example.TaskAPI.user.api.dto.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(config = BaseMapperConfig.class)
public interface TaskMapper {
    TaskResponse.Detail toResponse(Task task);

    TaskDetailResponse.Detail toTaskDetailResponse(TaskDetail taskDetail);

    List<TaskResponse.Detail> toResponseList(List<Task> tasks);

    @IgnoreBaseEntityMapping
    @IgnoreTaskMapping
    Task toEntity(TaskRequest.Detail request);

    @IgnoreBaseExtensionEntityMapping
    @IgnoreTaskDetailMapping
    TaskDetail toEntity(TaskDetailRequest.Detail request);

    @IgnoreBaseEntityMapping
    @IgnoreTaskMapping
    void update(Task source, @MappingTarget Task target);

    @IgnoreBaseExtensionEntityMapping
    @IgnoreTaskDetailMapping
    void update(TaskDetail source, @MappingTarget TaskDetail target);

    default Set<UserResponse.Summary> mapTaskAssignees(Set<TaskAssignee> taskAssignees) {
        return taskAssignees.stream()
                .map(taskAssignee -> UserResponse.Summary.builder()
                        .uuid(taskAssignee.getUser().getUuid())
                        .username(taskAssignee.getUser().getUsername())
                        .build())
                .collect(Collectors.toSet());
    }
}
