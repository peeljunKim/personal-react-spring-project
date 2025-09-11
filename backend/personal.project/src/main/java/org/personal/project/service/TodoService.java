package org.personal.project.service;

import org.personal.project.dto.PageRequestDTO;
import org.personal.project.dto.PageResponseDTO;
import org.personal.project.dto.TodoDTO;
import org.personal.project.entity.Todo;

public interface TodoService {

    public TodoDTO getOne(Long tno);

    public Long register(TodoDTO todoDTO);

    public void modify(TodoDTO todoDTO);

    public void remove(Long tno);

    PageResponseDTO<TodoDTO> list(PageRequestDTO pageRequestDTO);

    default TodoDTO entityToDTO(Todo todo) {

        return TodoDTO.builder()
                .tno(todo.getTno())
                .title(todo.getTitle())
                .dueDate(todo.getDueDate())
                .content(todo.getContent())
                .complete(todo.isComplete())
                .build();
    }

    default Todo dtoToEntity(TodoDTO todoDTO) {

        return Todo.builder()
                .tno(todoDTO.getTno())
                .title(todoDTO.getTitle())
                .dueDate(todoDTO.getDueDate())
                .content(todoDTO.getContent())
                .complete(todoDTO.isComplete())
                .build();
    }
}
