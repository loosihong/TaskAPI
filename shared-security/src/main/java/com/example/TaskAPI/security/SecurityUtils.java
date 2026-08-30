package com.example.TaskAPI.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {
    }

    public static JwtPrincipal getCurrentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null
                || !auth.isAuthenticated()
                || !(auth.getPrincipal() instanceof JwtPrincipal principal)) {
            throw new IllegalStateException("Authentication required");
        }

        return principal;
    }

    public static Long getCurrentUserId() {
        return getCurrentPrincipal().id();
    }
}
