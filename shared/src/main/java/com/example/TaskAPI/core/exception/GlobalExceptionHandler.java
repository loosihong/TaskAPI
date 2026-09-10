package com.example.TaskAPI.core.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("PMD.MoreThanOneLogger") // Deliberate: client/server error split, see logger field comments below.
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger CLIENT_ERROR = LoggerFactory.getLogger("com.example.TaskAPI.error.client");
    private static final Logger SERVER_ERROR = LoggerFactory.getLogger("com.example.TaskAPI.error.server");

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<String> handleOptimisticLocking(ObjectOptimisticLockingFailureException ex) {
        SERVER_ERROR.warn("Optimistic lock conflict", ex);

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Task was modified by another request. Please fetch the latest version and retry.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        CLIENT_ERROR.debug("Validation failed", ex);

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", 400);
        body.put("errors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
        CLIENT_ERROR.atDebug()
                .setMessage("Entity not found: {}")
                .addArgument(ex::getMessage)
                .setCause(ex)
                .log();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(DuplicateEntityException.class)
    public ResponseEntity<String> handleDuplicateEntity(DuplicateEntityException ex) {
        CLIENT_ERROR.atDebug()
                .setMessage("Duplicate entity: {}")
                .addArgument(ex::getMessage)
                .setCause(ex)
                .log();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentials(BadCredentialsException ex) {
        CLIENT_ERROR.debug("Authentication failed", ex);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
    }

    @ExceptionHandler(DataValidationException.class)
    public ResponseEntity<String> handleDataValidation(DataValidationException ex) {
        CLIENT_ERROR.atDebug()
                .setMessage("Data validation failed: {}")
                .addArgument(ex::getMessage)
                .setCause(ex)
                .log();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, Object>> handleMethodValidationErrors(
            HandlerMethodValidationException ex) {
        CLIENT_ERROR.debug("Parameter validation failed", ex);

        Map<String, String> parameterErrors = new HashMap<>();

        ex.getParameterValidationResults().forEach(result ->
                result.getResolvableErrors().forEach(error ->
                        parameterErrors.put(
                                result.getMethodParameter().getParameterName(),
                                error.getDefaultMessage())));

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", 400);
        body.put("errors", parameterErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericError(Exception ex) {
        String errorId = UUID.randomUUID().toString();

        SERVER_ERROR.atError()
                .setMessage("Unhandled exception")
                .addKeyValue("error.id", errorId)
                .setCause(ex)
                .log();

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", 500);
        body.put("error", "An unexpected error occurred");
        body.put("errorId", errorId);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
