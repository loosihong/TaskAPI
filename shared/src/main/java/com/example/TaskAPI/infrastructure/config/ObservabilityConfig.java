package com.example.TaskAPI.infrastructure.config;

import com.example.TaskAPI.core.observability.RequestLoggingFilter;
import com.example.TaskAPI.core.observability.UserMdcFilter;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class ObservabilityConfig {
    @Bean
    public FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilter() {
        FilterRegistrationBean<RequestLoggingFilter> registrationBean =
                new FilterRegistrationBean<>(new RequestLoggingFilter());

        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);

        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<UserMdcFilter> userMdcFilter() {
        FilterRegistrationBean<UserMdcFilter> registrationBean = new FilterRegistrationBean<>(new UserMdcFilter());

        registrationBean.setOrder(SecurityFilterProperties.DEFAULT_FILTER_ORDER + 10);

        return registrationBean;
    }
}
