package com.example.TaskAPI.task.mapper.annotation;

import org.mapstruct.Mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@Mapping(target = "taskId", ignore = true)
@Mapping(target = "task", ignore = true)
public @interface IgnoreTaskCommentMapping {
}
