package org.personal.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

//	public static void main(String[] args) {
//		SpringApplication application = new SpringApplication(Application.class);
//		application.setApplicationStartup(new BufferingApplicationStartup(2048));
//		application.run(args);
//	}

}
