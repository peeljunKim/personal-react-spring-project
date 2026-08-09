package org.personal.project.config;

import lombok.extern.log4j.Log4j2;
import org.personal.project.controller.formatter.LocalDateFormatter;
import org.personal.project.logging.HandlerLoggingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Log4j2
public class CustomServletConfig implements WebMvcConfigurer {

    private final HandlerLoggingInterceptor handlerLoggingInterceptor;

    @Autowired
    public CustomServletConfig(HandlerLoggingInterceptor handlerLoggingInterceptor) {
        this.handlerLoggingInterceptor = handlerLoggingInterceptor;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        log.info("addFormatters - LocalDateFormatter 시작");
        registry.addFormatter(new LocalDateFormatter());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Actuator 요청은 Filter에서만 기록하고 비즈니스 핸들러 정보 수집에서 제외
        registry.addInterceptor(handlerLoggingInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/actuator/**")
                .order(1);
    }

//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//
//        registry.addMapping("/**")
//                .allowedOrigins("*")
//                .allowedMethods("HEAD", "GET", "POST", "PUT", "DELETE", "OPTIONS")
//                .maxAge(300)
//                .allowedHeaders("Authorization", "Cache-Control", "Content-Type");
//    }

}
