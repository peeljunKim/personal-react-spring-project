package org.personal.project.repository;

import org.personal.project.entity.pg.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TradeRepository extends JpaRepository<Trade, Long> {

    Optional<Trade> findByTid(String tid);
}
