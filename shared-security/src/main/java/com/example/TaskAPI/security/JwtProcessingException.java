package com.example.TaskAPI.security;

public class JwtProcessingException extends RuntimeException {
    public JwtProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
