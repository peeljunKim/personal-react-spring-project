package org.personal.project.connectionPoolOptimization;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/testdb")
@RequiredArgsConstructor
public class DbHoldController {

    private final DbHoldRepository dbHoldRepository;

    @Transactional(readOnly = true)
    @GetMapping("/hold")
    public String hold(@RequestParam(defaultValue = "0.2") double sec) {
        dbHoldRepository.hold(sec);
        return "ok";
    }
}