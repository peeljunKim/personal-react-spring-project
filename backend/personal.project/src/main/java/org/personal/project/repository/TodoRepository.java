package org.personal.project.repository;

import org.personal.project.entity.Todo;
import org.personal.project.repository.search.TodoSearch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<Todo, Long>, TodoSearch {
}
