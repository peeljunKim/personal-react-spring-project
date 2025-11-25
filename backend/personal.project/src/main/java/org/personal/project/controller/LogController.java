package org.personal.project.controller;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api")
public class LogController {

    private final MeterRegistry meterRegistry;

    @GetMapping("/logTest")
    public void logTest() {
        log.info("info");
        log.error("error");
        log.trace("trace");
        log.warn("warn");
        log.debug("debug");

        Counter.builder("log.test")
                .tag("class", this.getClass().getName())
                .tag("method", "log")
                .description("log")
                .register(meterRegistry)
                .increment(); // 카운터 값 증가
    }
}
