package org.personal.project.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.personal.project.dto.page.PageRequestDTO;
import org.personal.project.dto.page.PageResponseDTO;
import org.personal.project.dto.TodoDTO;
import org.personal.project.entity.Todo;
import org.personal.project.repository.TodoRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
@Transactional
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;

    @Override
    public TodoDTO getOne(Long tno) {
        Optional<Todo> result = todoRepository.findById(tno);

        Todo todo = result.orElseThrow();

        return entityToDTO(todo);
    }

    @Override
    public Long register(TodoDTO todoDTO) {

        Todo todo = dtoToEntity(todoDTO);

        Todo result = todoRepository.save(todo);

        return result.getTno();
    }

    @Override
    public void modify(TodoDTO todoDTO) {

        Optional<Todo> result = todoRepository.findById(todoDTO.getTno());

        Todo todo = result.orElseThrow();

        todo.changeDueDate(todoDTO.getDueDate());
        todo.changeComplete(todoDTO.isComplete());
        todo.changeTitle(todoDTO.getTitle());
        todo.changeWriter(todoDTO.getWriter());

        todoRepository.save(todo);
    }

    @Override
    public void remove(Long tno) {

        todoRepository.deleteById(tno);

    }

    @Override
    public PageResponseDTO<TodoDTO> list(PageRequestDTO pageRequestDTO) {

        Page<Todo> result = todoRepository.search1(pageRequestDTO);

        List<TodoDTO> dtoList = result
                .get()
                .map(todo -> entityToDTO(todo)).collect(Collectors.toList());

        return PageResponseDTO.<TodoDTO>withAll()
                .dtoList(dtoList)
                .pageRequestDTO(pageRequestDTO)
                .totalCount(result.getTotalElements())
                .build();

//        modelMapper 사용
//        Pageable pageable = PageRequest.of(
//                pageRequestDTO.getPage() - 1,  // 1페이지가 0이므로 주의
//                pageRequestDTO.getSize(),
//                Sort.by("tno").descending());
//
//        Page<Todo> result = todoRepository.findAll(pageable);
//
//        List<TodoDTO> dtoList = result.getContent().stream()
//                .map(todo -> modelMapper.map(todo, TodoDTO.class))
//                .collect(Collectors.toList());
//
//        long totalCount = result.getTotalElements();
//
//        return PageResponseDTO.<TodoDTO>withAll()
//                .dtoList(dtoList)
//                .pageRequestDTO(pageRequestDTO)
//                .totalCount(totalCount)
//                .build();


    }
}
