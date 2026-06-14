package org.personal.project.repository;

import org.personal.project.entity.coupon.CouponTarget;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponTargetRepository extends JpaRepository<CouponTarget, Long> {
}
