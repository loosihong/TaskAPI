package com.example.TaskAPI.core;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SyncTaskExecutor;

import java.util.concurrent.Executor;

@TestConfiguration
public class SyncAuditExecutorConfig {
    @Bean(name = "auditExecutor")
    public Executor auditExecutor() {
        return new SyncTaskExecutor();
    }
}
