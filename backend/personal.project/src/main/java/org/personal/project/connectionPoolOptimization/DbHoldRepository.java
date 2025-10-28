package org.personal.project.connectionPoolOptimization;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface DbHoldRepository extends Repository<Dummy, Long> {

    @Query(value = "SELECT SLEEP(:sec)", nativeQuery = true)
    int hold(@Param("sec") double sec); //
}
