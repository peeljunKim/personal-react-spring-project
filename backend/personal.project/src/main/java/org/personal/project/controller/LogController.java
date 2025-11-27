package org.personal.project.controller;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@AllArgsConstructor
@RequestMapping("/api")
@Timed("log.test.timer") // timed도 내부적으로 count가 존재하기 때문에 value 값이 동일하면 count 값이 2가 증가함
public class LogController {

    private static final Logger log = LoggerFactory.getLogger(LogController.class);

    @Counted("log.test.count")
    @GetMapping("/logTest")
    public void logTest() {
        log.info("info");
        log.error("error");
        log.trace("trace");
        log.warn("warn");
        log.debug("debug");

//        Counter.builder("log.test")
//                .tag("class", this.getClass().getName())
//                .tag("method", "log")
//                .description("log")
//                .register(meterRegistry)
//                .increment(); // 카운터 값 증가

        // xxx/actuator/prometheus 해당 주소에서 메트릭 확인 가능
        // 그라파나에서 log_test_total{method="log"} 이런 식으로 사용하면 됨
    }
}
