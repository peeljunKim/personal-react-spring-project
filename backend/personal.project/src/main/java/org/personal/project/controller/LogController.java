package org.personal.project.controller;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Gauge;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j2
@RequestMapping("/api")
@Timed("log.test.timer") // timed도 내부적으로 count가 존재하기 때문에 value 값이 동일하면 count 값이 2가 증가함
public class LogController {

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
