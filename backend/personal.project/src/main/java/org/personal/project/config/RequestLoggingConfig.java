package org.personal.project.config;

import org.personal.project.logging.RequestLoggingFilter;
import org.personal.project.logging.TraceContextAccessor;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RequestLoggingConfig {

    @Bean
    public FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilter(
            TraceContextAccessor traceContextAccessor
    ) {
        FilterRegistrationBean<RequestLoggingFilter> registration =
                new FilterRegistrationBean<>(new RequestLoggingFilter(traceContextAccessor));
        registration.setName("requestLoggingFilter");
        registration.addUrlPatterns("/*");
        // 401/403 기록과 Spring HTTP Observation span 사용을 위한 실행 순서
        registration.setOrder(SecurityProperties.DEFAULT_FILTER_ORDER - 1);
        return registration;
    }
}
