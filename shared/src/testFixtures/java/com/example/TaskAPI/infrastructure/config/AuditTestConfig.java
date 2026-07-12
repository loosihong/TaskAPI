package com.example.TaskAPI.infrastructure.config;

import com.example.TaskAPI.core.audit.ReflectionAuditListener;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@TestConfiguration
public class AuditTestConfig {
    private static final ThreadLocal<Long> CURRENT_AUDITOR = ThreadLocal.withInitial(() -> 1L);

    public static void setCurrentAuditor(Long userId) {
        CURRENT_AUDITOR.set(userId);
    }

    public static void resetCurrentAuditor() {
        CURRENT_AUDITOR.remove();
    }

    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> Optional.ofNullable(CURRENT_AUDITOR.get());
    }

    @EventListener(ContextRefreshedEvent.class)
    public void initaliseAuditListener(ContextRefreshedEvent event) {
        ApplicationEventPublisher eventPublisher = event.getApplicationContext();
        AuditorAware<Long> auditorAware = event.getApplicationContext().getBean(AuditorAware.class);
        new ReflectionAuditListener().init(eventPublisher, auditorAware);
    }
}
