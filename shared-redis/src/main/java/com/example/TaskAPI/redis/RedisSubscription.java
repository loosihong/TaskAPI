package com.example.TaskAPI.redis;

import org.springframework.data.redis.connection.MessageListener;

public record RedisSubscription(
        String channel,
        MessageListener listener
) {
}
