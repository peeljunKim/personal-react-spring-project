package org.personal.project.service.coupon;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.personal.project.dto.coupon.response.CouponIssueResponse;
import org.personal.project.entity.Member;
import org.personal.project.entity.coupon.CouponIssueType;
import org.personal.project.entity.coupon.CouponPolicy;
import org.personal.project.entity.coupon.CouponPolicyStatus;
import org.personal.project.entity.coupon.MemberCoupon;
import org.personal.project.entity.coupon.MemberCouponStatus;
import org.personal.project.repository.coupon.CouponPolicyRepository;
import org.personal.project.repository.coupon.MemberCouponRepository;
import org.personal.project.repository.MemberRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 쿠폰 발급 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CouponIssueService {

    private static final String ISSUE_SUCCESS = "SUCCEEDED";
    private static final String ISSUE_FAILED = "FAILED";

    private final CouponPolicyRepository couponPolicyRepository;
    private final MemberCouponRepository memberCouponRepository;
    private final MemberRepository memberRepository;
    private final CouponIssueHistoryRecorder issueHistoryRecorder;

    /**
     * 일반 쿠폰 발급 요청 처리
     */
    @Transactional
    public CouponIssueResponse issueCoupon(Long policyId, String memberId) {
        log.info("쿠폰 발급 시작 policyId={}, memberId={}", policyId, memberId);
        MemberCoupon memberCoupon = validateCoupon(policyId, memberId, null, CouponIssueType.GENERAL);

        log.info("쿠폰 발급 완료 policyId={}, memberId={}, memberCouponId={}",
                policyId, memberId, memberCoupon.getMemberCouponId());

        return new CouponIssueResponse(
                memberCoupon.getMemberCouponId(),
                policyId,
                memberCoupon.getStatus().name(),
                "쿠폰 발급 성공"
        );
    }

    /**
     * 선착순 쿠폰 발급로 처리
     */
    @Transactional
    public MemberCoupon issueFCFSCoupon(Long policyId, String memberId, String requestKey) {
        return validateCoupon(policyId, memberId, requestKey, CouponIssueType.FIRST_COME_FIRST_SERVED);
    }

    /**
     * 쿠폰 발급 내부 처리(유효성 검사)
     */
    private MemberCoupon validateCoupon(
            Long policyId,
            String memberId,
            String requestKey,
            CouponIssueType expectedIssueType
    ) {
        log.info("쿠폰 발급 처리 시작 policyId={}, memberId={}, requestKey={}", policyId, memberId, requestKey);

        CouponPolicy policy = couponPolicyRepository.findById(policyId)
                .orElseThrow(() -> new CouponException("쿠폰 정책을 찾을 수 없습니다. policyId=" + policyId));

        try {
            validateIssueType(policy, expectedIssueType);
            validateOneTimeIssuePolicy(policy);

            if (memberCouponRepository.findByPolicyPolicyIdAndMemberEmail(policyId, memberId).isPresent()) {
                log.warn("쿠폰 중복 발급 차단 policyId={}, memberId={}, requestKey={}", policyId, memberId, requestKey);
                throw new CouponException("이미 발급받은 쿠폰입니다.");
            }

            LocalDateTime now = LocalDateTime.now();
            int increased = couponPolicyRepository.increaseIssuedCountIfAvailable(
                    policyId,
                    CouponPolicyStatus.ACTIVE,
                    now
            );

            if (increased != 1) {
                log.warn("쿠폰 발급 수량 확보 실패 policyId={}, memberId={}, requestKey={}",
                        policyId, memberId, requestKey);
                throw new CouponException("발급 가능한 쿠폰이 아닙니다.");
            }

            Member member = memberRepository.getReferenceById(memberId);
            MemberCoupon memberCoupon = memberCouponRepository.saveAndFlush(MemberCoupon.builder()
                    .policy(policy)
                    .member(member)
                    .status(MemberCouponStatus.ISSUED)
                    .build());

            issueHistoryRecorder.record(
                    policyId,
                    policy.getName(),
                    policy.getDiscountAmount(),
                    memberId,
                    requestKey,
                    ISSUE_SUCCESS,
                    null
            );
            log.info("쿠폰 발급 저장 완료 policyId={}, memberId={}, requestKey={}, memberCouponId={}",
                    policyId, memberId, requestKey, memberCoupon.getMemberCouponId());

            return memberCoupon;
        } catch (DataIntegrityViolationException e) {
            issueHistoryRecorder.record(
                    policyId,
                    policy.getName(),
                    policy.getDiscountAmount(),
                    memberId,
                    requestKey,
                    ISSUE_FAILED,
                    "DUPLICATED_ISSUE"
            );
            log.warn("쿠폰 발급 DB 제약 조건 충돌 policyId={}, memberId={}, requestKey={}",
                    policyId, memberId, requestKey);
            throw new CouponException("이미 발급받은 쿠폰입니다.", e);
        } catch (CouponException e) {
            issueHistoryRecorder.record(
                    policyId,
                    policy.getName(),
                    policy.getDiscountAmount(),
                    memberId,
                    requestKey,
                    ISSUE_FAILED,
                    e.getMessage()
            );
            log.warn("쿠폰 발급 실패 policyId={}, memberId={}, requestKey={}, reason={}",
                    policyId, memberId, requestKey, e.getMessage());
            throw e;
        }
    }

    /**
     * 발급 방식 검증
     */
    private void validateIssueType(CouponPolicy policy, CouponIssueType expectedIssueType) {
        if (policy.getIssueType() != expectedIssueType) {
            throw new CouponException("쿠폰 발급 방식이 올바르지 않습니다.");
        }
    }

    /**
     * 사용자별 1회 발급 정책 검증
     */
    private void validateOneTimeIssuePolicy(CouponPolicy policy) {
        if (policy.getPerMemberIssueLimit() != 1) {
            throw new CouponException("현재는 사용자별 1회 발급 쿠폰만 지원합니다.");
        }
    }
}
