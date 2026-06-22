package org.personal.project.repository.coupon;

import org.personal.project.entity.coupon.CouponTarget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CouponTargetRepository extends JpaRepository<CouponTarget, Long> {

    /**
     * 정책별 적용 대상 조회
     */
    List<CouponTarget> findByPolicyPolicyId(Long policyId);

    /**
     * 여러 정책 적용 대상 조회
     */
    List<CouponTarget> findByPolicyPolicyIdIn(Collection<Long> policyIds);

    /**
     * 정책별 적용 대상 삭제
     */
    void deleteByPolicyPolicyId(Long policyId);
}
