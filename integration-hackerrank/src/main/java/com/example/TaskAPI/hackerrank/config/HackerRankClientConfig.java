package com.example.TaskAPI.hackerrank.config;

import com.example.TaskAPI.hackerrank.client.HackerRankArticleClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration(proxyBeanMethods = false)
@ImportHttpServices(group = "hackerrank", types = HackerRankArticleClient.class)
public class HackerRankClientConfig {
}
