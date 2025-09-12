package org.personal.project.repository;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.personal.project.entity.Todo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Log4j2
public class TodoRepositoryTests {

    @Autowired
    private TodoRepository todoRepository;

    @Test
    void repo주입_확인() {
        assertNotNull(todoRepository);
        log.info(todoRepository.getClass().getName());
    }

    @Test
    void insertTest() {

        for (int i = 0; i < 100; i++) {
            Todo todo = Todo.builder()
                    .title("title_" + i)
                    .writer("content")
                    .dueDate(LocalDate.of(2025, 10, 1))
                    .build();

            Todo result = todoRepository.save(todo);
            log.info(result);
        }

    }

    @Test
    void readTest() {
        Long tno = 1L;

        Optional<Todo> result = todoRepository.findById(tno);

        Todo todo = result.orElseThrow();

        log.info(todo);
    }

    @Test
    void updateTest() {
        Long tno = 1L;

        Optional<Todo> result = todoRepository.findById(tno);

        Todo todo = result.orElseThrow();

        todo.changeTitle("update title");
        todo.changeContent("update content");
        todo.changeComplete(true);
        todo.changeDueDate(LocalDate.of(2025, 12, 31));

        Todo save = todoRepository.save(todo);

        log.info(save);

    }

    @Test
    void pagingTest() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("tno").descending());

        Page<Todo> result = todoRepository.findAll(pageable);

        log.info(result.getTotalElements());
        log.info(result.getContent());
    }

//    @Test
//    void search1Test() {
//        Page<Todo> todos = todoRepository.search1();
//    }
}
