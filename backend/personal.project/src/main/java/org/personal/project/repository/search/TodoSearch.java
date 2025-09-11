package org.personal.project.repository.search;

import org.personal.project.dto.PageRequestDTO;
import org.personal.project.entity.Todo;
import org.springframework.data.domain.Page;

public interface TodoSearch {

    Page<Todo> search1(PageRequestDTO pageRequestDTO);
}
