package org.personal.project.repository;

import org.personal.project.entity.pg.PaymentCompensation;
import org.personal.project.entity.pg.PaymentCompensationStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PaymentCompensationRepository extends JpaRepository<PaymentCompensation, Long> {

    List<PaymentCompensation> findByStatusInOrderByLastTriedAtAscCreatedAtAsc(
            Collection<PaymentCompensationStatus> statuses,
            Pageable pageable
    );
}
