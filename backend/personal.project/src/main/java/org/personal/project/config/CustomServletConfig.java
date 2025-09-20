package org.personal.project.config;

import lombok.extern.log4j.Log4j2;
import org.personal.project.controller.formatter.LocalDateFormatter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Log4j2
public class CustomServletConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {

        log.info("addFormatters - LocalDateFormatter 시작");
        registry.addFormatter(new LocalDateFormatter());
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
