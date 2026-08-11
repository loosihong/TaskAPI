package com.example.TaskAPI.task.domain.enums;

import jakarta.persistence.AttributeConverter;
import lombok.Getter;

@Getter
public enum TaskStatus {
    TODO("TODO"),
    IN_PROGRESS("IN_PROGRESS"),
    DONE("DONE"),
    OVERDUE("OVERDUE"),
    CANCELLED("CANCELLED");

    private final String code;

    TaskStatus(String code) {
        this.code = code;
    }

    public static TaskStatus fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        for (TaskStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }

        throw new IllegalArgumentException("Unknown TaskStatus code: " + code);
    }

    @jakarta.persistence.Converter
    public static class Converter implements AttributeConverter<TaskStatus, String> {
        @Override
        public String convertToDatabaseColumn(TaskStatus status) {
            return status == null ? null : status.getCode();
        }

        @Override
        public TaskStatus convertToEntityAttribute(String value) {
            return fromCode(value);
        }
    }
}
