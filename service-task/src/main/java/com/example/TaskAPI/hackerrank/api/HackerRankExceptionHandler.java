package com.example.TaskAPI.hackerrank.api;

import com.example.TaskAPI.hackerrank.exception.HackerRankApiException;
import com.example.TaskAPI.hackerrank.exception.HackerRankUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class HackerRankExceptionHandler {
    @ExceptionHandler(HackerRankUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleUpstreamUnavailable(HackerRankUnavailableException ex) {
        log.warn("HackerRank is unavailable", ex);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .header(HttpHeaders.RETRY_AFTER, "30")
                .body(errorBody(HttpStatus.SERVICE_UNAVAILABLE, "Upstream integration unavailable"));
    }

    @ExceptionHandler(HackerRankApiException.class)
    public ResponseEntity<Map<String, Object>> handleUpstreamRejection(HackerRankApiException ex) {
        log.error("HackerRank rejected the request", ex);

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(errorBody(HttpStatus.BAD_GATEWAY, "Upstream integration failed"));
    }

    private Map<String, Object> errorBody(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();

        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status);
        body.put("error", message);

        return body;
    }
}
