package com.example.TaskAPI;

import com.example.TaskAPI.infrastructure.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(AppProperties.class)
@SpringBootApplication
public class TaskApiApplication {

    static void main(String[] args) {
        SpringApplication.run(TaskApiApplication.class, args);
    }
}