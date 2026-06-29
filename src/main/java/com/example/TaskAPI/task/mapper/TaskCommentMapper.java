package com.example.TaskAPI.task.mapper;

import com.example.TaskAPI.core.mapper.BaseMapperConfig;
import com.example.TaskAPI.core.mapper.annotation.IgnoreBaseEntityMapping;
import com.example.TaskAPI.task.api.dto.TaskCommentRequest;
import com.example.TaskAPI.task.api.dto.TaskCommentResponse;
import com.example.TaskAPI.task.domain.entity.TaskComment;
import com.example.TaskAPI.task.mapper.annotation.IgnoreTaskCommentMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = BaseMapperConfig.class)
public interface TaskCommentMapper {
    TaskCommentResponse.Detail toResponse(TaskComment taskComment);

    @IgnoreBaseEntityMapping
    @IgnoreTaskCommentMapping
    TaskComment toEntity(TaskCommentRequest.Detail taskCommentRequest);

    @IgnoreBaseEntityMapping
    @IgnoreTaskCommentMapping
    void update(TaskComment source, @MappingTarget TaskComment target);
}
