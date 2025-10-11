package org.personal.project.fix;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@Log4j2
@RequiredArgsConstructor
public class JsonController {

    private final RestTemplate restTemplate;

    @GetMapping("/jackson")
    public String testJackson() {
        String url = "https://jsonplaceholder.typicode.com/posts/1";
        long totalTime = 0;
        int reps = 1000;

        for (int i = 0; i < reps; i++) {
            long start = System.nanoTime();
            JacksonDTO result = restTemplate.getForObject(url, JacksonDTO.class);

            long end = System.nanoTime();
            long elapsedMs = (end - start) / 1_000_000; // 밀리초 변환
            totalTime += elapsedMs;
        }

        double average = (double) totalTime / reps;
        log.info("총 {}회 평균 응답 시간: {} ms", reps, average);

        return "완료";
    }
}
