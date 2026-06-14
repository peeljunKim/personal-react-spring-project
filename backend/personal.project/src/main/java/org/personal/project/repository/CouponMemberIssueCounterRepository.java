package org.personal.project.repository;

import org.personal.project.entity.coupon.CouponMemberIssueCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CouponMemberIssueCounterRepository extends JpaRepository<CouponMemberIssueCounter, Long> {

    /**
     * 사용자별 발급 카운터 조회
     * <p>N회 발급 제한 확인용</p>
     */
    Optional<CouponMemberIssueCounter> findByPolicyPolicyIdAndMemberEmail(Long policyId, String memberId);

    /**
     * 사용자별 발급 카운트 증가
     * <p>현재 카운트가 제한보다 낮을 때만 증가</p>
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update CouponMemberIssueCounter c
               set c.issuedCount = c.issuedCount + 1,
                   c.version = c.version + 1
             where c.policy.policyId = :policyId
               and c.member.email = :memberId
               and c.issuedCount < :limit
            """)
    int increaseIfUnderLimit(
            @Param("policyId") Long policyId,
            @Param("memberId") String memberId,
            @Param("limit") int limit
    );
}
