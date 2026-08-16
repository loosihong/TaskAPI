package com.example.TaskAPI.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "TaskAPI - Task Service", version = "v1"),
        security = @SecurityRequirement(name = "bearerAuth")
)
public class OpenApiConfig {
}
