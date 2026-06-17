package org.personal.project.service.coupon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.personal.project.dto.coupon.CouponIssueMessage;
import org.personal.project.dto.coupon.response.CouponIssueStatusResponse;
import org.personal.project.dto.coupon.response.EventCouponIssueResponse;
import org.personal.project.entity.Member;
import org.personal.project.entity.coupon.CouponIssueOutbox;
import org.personal.project.entity.coupon.CouponIssueOutboxStatus;
import org.personal.project.entity.coupon.CouponIssueRequest;
import org.personal.project.entity.coupon.CouponIssueRequestStatus;
import org.personal.project.entity.coupon.CouponIssueType;
import org.personal.project.entity.coupon.CouponPolicy;
import org.personal.project.repository.coupon.CouponIssueOutboxRepository;
import org.personal.project.repository.coupon.CouponIssueRequestRepository;
import org.personal.project.repository.coupon.CouponPolicyRepository;
import org.personal.project.repository.MemberRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 이벤트 쿠폰 발급 요청 관리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventCouponIssueService {

    private final CouponIssueRequestRepository couponIssueRequestRepository;
    private final CouponIssueOutboxRepository couponIssueOutboxRepository;
    private final CouponPolicyRepository couponPolicyRepository;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    /**
     * 이벤트 쿠폰 발급 요청 멱등 접수
     */
    @Transactional
    public EventCouponIssueResponse requestIssue(Long policyId, String memberId, String requestKey) {
        log.info("이벤트 쿠폰 발급 요청 저장 시작 policyId={}, memberId={}, requestKey={}",
                policyId, memberId, requestKey);

        return couponIssueRequestRepository.findByRequestKey(requestKey)
                .map(request -> {
                    log.info("이벤트 쿠폰 발급 요청 멱등 응답 policyId={}, memberId={}, requestKey={}, status={}",
                            request.getPolicy().getPolicyId(),
                            request.getMember().getEmail(),
                            request.getRequestKey(),
                            request.getStatus());
                    return toEventResponse(request);
                })
                .orElseGet(() -> createIssueRequest(policyId, memberId, requestKey));
    }

    /**
     * 이벤트 쿠폰 발급 요청 상태 조회
     */
    @Transactional(readOnly = true)
    public CouponIssueStatusResponse getIssueStatus(String requestKey) {
        log.info("이벤트 쿠폰 발급 요청 조회 시작 requestKey={}", requestKey);
        CouponIssueRequest request = couponIssueRequestRepository.findByRequestKey(requestKey)
                .orElseThrow(() -> new CouponException("쿠폰 발급 요청을 찾을 수 없습니다. requestKey=" + requestKey));

        log.info("이벤트 쿠폰 발급 요청 상태 조회 완료 requestKey={}, status={}, failureReason={}",
                request.getRequestKey(), request.getStatus(), request.getFailureReason());

        return new CouponIssueStatusResponse(
                request.getRequestKey(),
                request.getPolicy().getPolicyId(),
                request.getMember().getEmail(),
                request.getStatus().name(),
                request.getFailureReason()
        );
    }

    /**
     * 발급 요청과 Outbox 신규 생성
     */
    private EventCouponIssueResponse createIssueRequest(Long policyId, String memberId, String requestKey) {
        try {
            CouponPolicy policy = couponPolicyRepository.findById(policyId)
                    .orElseThrow(() -> new CouponException("쿠폰 정책을 찾을 수 없습니다. policyId=" + policyId));
            validateFirstComePolicy(policy);
            Member member = memberRepository.getReferenceById(memberId);
            CouponIssueMessage message = new CouponIssueMessage(requestKey, policyId, memberId);

            log.info("이벤트 쿠폰 발급 요청 생성 policyId={}, memberId={}, requestKey={}",
                    policyId, memberId, requestKey);
            CouponIssueRequest request = couponIssueRequestRepository.saveAndFlush(CouponIssueRequest.builder()
                    .requestKey(requestKey)
                    .policy(policy)
                    .member(member)
                    .status(CouponIssueRequestStatus.PENDING)
                    .build());

            couponIssueOutboxRepository.saveAndFlush(CouponIssueOutbox.builder()
                    .requestKey(requestKey)
                    .policyId(policyId)
                    .memberId(memberId)
                    .status(CouponIssueOutboxStatus.PENDING)
                    .payload(toPayload(message))
                    .build());

            log.info("이벤트 쿠폰 Outbox 생성 policyId={}, memberId={}, requestKey={}",
                    policyId, memberId, requestKey);

            return toEventResponse(request);
        } catch (DataIntegrityViolationException e) {
            CouponIssueRequest request = couponIssueRequestRepository.findByRequestKey(requestKey)
                    .orElseThrow(() -> new CouponException("쿠폰 발급 요청 멱등 처리에 실패했습니다.", e));

            log.warn("이벤트 쿠폰 발급 요청 중복 저장 충돌 policyId={}, memberId={}, requestKey={}, status={}",
                    policyId, memberId, requestKey, request.getStatus());

            return toEventResponse(request);
        }
    }

    /**
     * 선착순 쿠폰 정책 검증
     */
    private void validateFirstComePolicy(CouponPolicy policy) {
        if (policy.getIssueType() != CouponIssueType.FIRST_COME_FIRST_SERVED) {
            throw new CouponException("선착순 쿠폰 정책이 아닙니다.");
        }
    }

    /**
     * 발급 메시지 JSON 변환
     */
    private String toPayload(CouponIssueMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new CouponException("쿠폰 발급 메시지 생성에 실패했습니다.", e);
        }
    }

    /**
     * 발급 요청 응답 변환
     */
    private EventCouponIssueResponse toEventResponse(CouponIssueRequest request) {
        return new EventCouponIssueResponse(
                request.getRequestKey(),
                request.getPolicy().getPolicyId(),
                request.getStatus().name(),
                "쿠폰 발급 요청 접수"
        );
    }
}
