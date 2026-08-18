package com.example.TaskAPI.hackerrank.config;

import com.example.TaskAPI.hackerrank.client.HackerRankArticleClient;
import com.example.TaskAPI.hackerrank.exception.HackerRankApiException;
import com.example.TaskAPI.hackerrank.exception.HackerRankUnavailableException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration(proxyBeanMethods = false)
@ImportHttpServices(group = "hackerrank", types = HackerRankArticleClient.class)
public class HackerRankClientConfig {
    private static final int MAX_ERROR_BODY_BYTES = 512;

    public static void customize(RestClient.Builder clientBuilder) {
        clientBuilder
                .defaultHeader("User-Agent", "TaskAPI/1.0")
                .defaultStatusHandler(HttpStatusCode::is5xxServerError,
                        (request, response) -> {
                            throw new HackerRankUnavailableException(
                                    "HackerRank " + response.getStatusCode() + ": " + getResponseBody(response),
                                    null);
                        })
                .defaultStatusHandler(HttpStatusCode::is4xxClientError,
                        (request, response) -> {
                            throw new HackerRankApiException(
                                    "HackerRank " + response.getStatusCode() + ": " + getResponseBody(response),
                                    null);
                        });
    }

    private static String getResponseBody(ClientHttpResponse response) throws IOException {
        return new String(
                response.getBody().readNBytes(MAX_ERROR_BODY_BYTES),
                StandardCharsets.UTF_8);
    }

    @Bean
    RestClientHttpServiceGroupConfigurer groupConfigurer() {
        return groups -> groups.filterByName("hackerrank")
                .forEachClient((group, clientBuilder) -> customize(clientBuilder));
    }
}
