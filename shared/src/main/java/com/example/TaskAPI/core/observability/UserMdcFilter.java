package com.example.TaskAPI.core.observability;

import com.example.TaskAPI.core.audit.AuditablePrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class UserMdcFilter extends OncePerRequestFilter {
    @NotNull
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null
                && auth.isAuthenticated()
                && auth.getPrincipal() instanceof AuditablePrincipal principal) {
            MDC.put(RequestLoggingFilter.USER_ID, String.valueOf(principal.auditorId()));
        }

        filterChain.doFilter(request, response);
    }
}
