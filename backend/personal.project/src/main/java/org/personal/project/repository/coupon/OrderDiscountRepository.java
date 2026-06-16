package org.personal.project.repository;

import org.personal.project.entity.coupon.OrderDiscount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDiscountRepository extends JpaRepository<OrderDiscount, Long> {
}
