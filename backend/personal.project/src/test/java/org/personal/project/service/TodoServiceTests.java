package org.personal.project.service;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.personal.project.dto.PageRequestDTO;
import org.personal.project.dto.PageResponseDTO;
import org.personal.project.dto.TodoDTO;
import org.personal.project.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Log4j2
class TodoServiceTests {

    @Autowired
    TodoService todoService;

    @Autowired
    TodoRepository todoRepository;


    @Test
    void getOneTest() {
        Long tno = 10L;

        log.info(todoService.getOne(tno));
    }

    @Test
    void registerTest() {

        TodoDTO todoDTO = TodoDTO.builder()
                .title("서비스 테스트")
                .content("test")
                .dueDate(LocalDate.of(2023, 10, 10))
                .build();


        log.info(todoService.register(todoDTO));
    }


    @Test
    public void listTest() {

        PageRequestDTO pageRequestDTO = PageRequestDTO
                .builder()
                .build();


        log.info(todoService.list(pageRequestDTO));

    }
}