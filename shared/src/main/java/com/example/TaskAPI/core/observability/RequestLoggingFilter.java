package com.example.TaskAPI.core.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.util.UUID;

public class RequestLoggingFilter extends OncePerRequestFilter {
    public static final String REQUEST_ID = "requestId";
    public static final String USER_ID = "userId";

    private static final String HEADER_REQUEST_ID = "X-Request-Id";
    // Routing key for the ACCESS_FILE / ACCESS_ASYNC appenders in shared/.../logging/.
    // Must remain a string literal — do not convert to getLogger(RequestLoggingFilter.class).
    private static final Logger ACCESS = LoggerFactory.getLogger("com.example.TaskAPI.access");

    @NullMarked
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long startNanos = System.nanoTime();
        String rawRequestId = request.getHeader(HEADER_REQUEST_ID);
        String requestId;

        if (rawRequestId != null) {
            try {
                requestId = UUID.fromString(rawRequestId).toString();
            } catch (IllegalArgumentException ex) {
                requestId = UUID.randomUUID().toString();
            }
        } else {
            requestId = UUID.randomUUID().toString();
        }

        MDC.put(REQUEST_ID, requestId);
        response.setHeader(HEADER_REQUEST_ID, requestId);
        SqlStats.start();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - startNanos) / 1_000_000L;

            ACCESS.atInfo()
                    .setMessage("http_request")
                    .addKeyValue("http.method", request.getMethod())
                    .addKeyValue("url.path", request.getRequestURI())
                    .addKeyValue("http.route", route(request))
                    .addKeyValue("http.status", response.getStatus())
                    .addKeyValue("event.duration_ms", durationMs)
                    .addKeyValue("db.query_count", SqlStats.count())
                    .addKeyValue("db.duration_ms", SqlStats.totalMillis())
                    .addKeyValue("client.ip", request.getRemoteAddr())
                    .log();

            SqlStats.clear();
            MDC.remove(REQUEST_ID);
            MDC.remove(USER_ID);
        }
    }

    private String route(HttpServletRequest request) {
        Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

        return pattern != null ? pattern.toString() : "UNMATCHED";
    }
}
