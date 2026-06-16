package org.personal.project.repository;

import org.personal.project.entity.coupon.CouponUsageHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponUsageHistoryRepository extends JpaRepository<CouponUsageHistory, Long> {
}
