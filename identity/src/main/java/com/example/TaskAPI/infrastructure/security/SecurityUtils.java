package com.example.TaskAPI.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {
    }

    public static CustomUserDetails getCurrentUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null
                || !auth.isAuthenticated()
                || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            throw new IllegalStateException("Authentication required");
        }

        return (CustomUserDetails) auth.getPrincipal();
    }

    public static Long getCurrentUserId() {
        return getCurrentUserDetails().getId();
    }
}
