package org.personal.project.repository;

import org.personal.project.entity.coupon.CouponIssueHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponIssueHistoryRepository extends JpaRepository<CouponIssueHistory, Long> {
}
