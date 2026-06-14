package org.personal.project.repository;

import org.personal.project.entity.coupon.CouponPolicy;
import org.personal.project.entity.coupon.CouponPolicyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface CouponPolicyRepository extends JpaRepository<CouponPolicy, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update CouponPolicy p
               set p.issuedCount = p.issuedCount + 1,
                   p.version = p.version + 1
             where p.policyId = :policyId
               and p.status = :activeStatus
               and p.issueStartAt <= :now
               and p.issueEndAt > :now
               and p.issuedCount < p.totalIssueLimit
            """)
    int increaseIssuedCountIfAvailable(
            @Param("policyId") Long policyId,
            @Param("activeStatus") CouponPolicyStatus activeStatus,
            @Param("now") LocalDateTime now
    );
}
