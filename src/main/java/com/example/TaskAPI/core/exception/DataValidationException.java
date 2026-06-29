package com.example.TaskAPI.core.exception;

public class DataValidationException extends RuntimeException {
    public <T> DataValidationException(Class<T> classType, String message) {
        super(classType.getSimpleName() + ": " + message);
    }
}
