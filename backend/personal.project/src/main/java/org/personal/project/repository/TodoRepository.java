package org.personal.project.repository;

import org.personal.project.entity.Todo;
import org.personal.project.repository.search.TodoSearch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface TodoRepository extends JpaRepository<Todo, Long>, TodoSearch {
}
