package com.example.TaskAPI.task.domain.enums;

import jakarta.persistence.AttributeConverter;

public enum Priority {
    LOW(1),
    MEDIUM(2),
    HIGH(3);

    private final short value;

    Priority(int value) {
        this.value = (short) value;
    }

    public static Priority fromValue(short value) {
        for (Priority priority : values()) {
            if (priority.value == value) return priority;
        }

        throw new IllegalArgumentException("Unknown Priority value: " + value);
    }

    public short getValue() {
        return value;
    }

    @jakarta.persistence.Converter(autoApply = true)
    public static class Converter implements AttributeConverter<Priority, Short> {
        @Override
        public Short convertToDatabaseColumn(Priority priority) {
            return priority == null ? null : priority.getValue();
        }

        @Override
        public Priority convertToEntityAttribute(Short value) {
            return value == null ? null : Priority.fromValue(value);
        }
    }
}
