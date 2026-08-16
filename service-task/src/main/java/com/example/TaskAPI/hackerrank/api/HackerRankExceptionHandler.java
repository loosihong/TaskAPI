package com.example.TaskAPI.hackerrank.api;

import com.example.TaskAPI.hackerrank.exception.HackerRankApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
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
    @ExceptionHandler(HackerRankApiException.class)
    public ResponseEntity<Map<String, Object>> handleUpstreamFailure(HackerRankApiException ex) {
        log.warn("HackerRank upstream call failed", ex);

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", 502);
        body.put("error", "Upstream integration unavailable");

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }
}
