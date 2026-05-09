package org.personal.project.repository;

import org.personal.project.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
            select oi.product.pno
            from OrderItem oi
            where oi.order.paymentId = :paymentId
            order by oi.product.pno asc
            """)
    List<Long> findProductIdsByPaymentId(@Param("paymentId") String paymentId);
}
