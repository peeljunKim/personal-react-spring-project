package org.personal.project.repository;

import org.personal.project.entity.pg.PaymentRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, Long> {
}
