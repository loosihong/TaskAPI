package com.example.TaskAPI.task.mapper.annotation;

import org.mapstruct.Mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@Mapping(target = "taskDetail", ignore = true)
@Mapping(target = "taskComments", ignore = true)
@Mapping(target = "taskAssignees", ignore = true)
public @interface IgnoreTaskMapping {
}
