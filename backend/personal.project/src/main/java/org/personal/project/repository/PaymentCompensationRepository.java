package org.personal.project.repository;

import org.personal.project.entity.pg.PaymentCompensation;
import org.personal.project.entity.pg.PaymentCompensationStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PaymentCompensationRepository extends JpaRepository<PaymentCompensation, Long> {

    /**
     * 보상 재시도 대상 조회
     * <p>PG 취소 실패 등 재처리에서 사용</p>
     */
    List<PaymentCompensation> findByStatusInOrderByLastTriedAtAscCreatedAtAsc(
            Collection<PaymentCompensationStatus> statuses,
            Pageable pageable
    );
}
