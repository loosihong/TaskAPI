package com.example.TaskAPI.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@AutoConfigureMockMvc
public class BaseWebIntegrationTest extends BaseIntegrationTest {
    @Autowired
    protected MockMvc mockMvc;

    protected RequestPostProcessor authenticated() {
        return request -> {
            request.addHeader("Authorization", "Bearer " + testUserToken);
            return request;
        };
    }
}
