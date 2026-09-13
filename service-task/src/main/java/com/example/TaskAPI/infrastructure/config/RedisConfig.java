package com.example.TaskAPI.infrastructure.config;

import com.example.TaskAPI.task.stream.TaskChangeSubscriber;
import com.example.TaskAPI.task.stream.TaskEventChannel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "taskapi.stream", name = "broadcaster", havingValue = "redis")
public class RedisConfig {
    @Bean
    public ThreadPoolTaskExecutor redisListenerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("redis-listener-");
        executor.initialize();

        return executor;
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            TaskChangeSubscriber taskChangeSubscriber,
            ThreadPoolTaskExecutor redisListenerExecutor
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();

        container.setConnectionFactory(connectionFactory);
        container.setTaskExecutor(redisListenerExecutor);
        container.addMessageListener(taskChangeSubscriber, new ChannelTopic(TaskEventChannel.NAME));

        return container;
    }
}
