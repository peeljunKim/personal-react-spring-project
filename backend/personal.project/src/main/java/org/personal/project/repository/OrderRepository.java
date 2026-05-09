package org.personal.project.repository;

import org.personal.project.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByPaymentId(String paymentId);

    @Query("""
            select distinct o
            from Order o
            left join fetch o.items oi
            left join fetch oi.product
            where o.paymentId = :paymentId
            """)
    Optional<Order> findByPaymentIdWithItems(@Param("paymentId") String paymentId);
}
