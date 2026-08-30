package com.example.TaskAPI.security;

import com.example.TaskAPI.core.audit.AuditablePrincipal;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

public record JwtPrincipal(
        Long id,
        String username,
        Collection<? extends GrantedAuthority> authorities
) implements AuthenticatedPrincipal, AuditablePrincipal {
    public JwtPrincipal {
        authorities = authorities == null ? List.of() : List.copyOf(authorities);
    }

    @NullMarked
    @Override
    public String getName() {
        return username;
    }

    @Override
    public Long auditorId() {
        return id;
    }
}
