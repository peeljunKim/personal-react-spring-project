package org.personal.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.personal.project.dto.coupon.response.CouponIssueResponse;
import org.personal.project.dto.coupon.response.CouponIssueStatusResponse;
import org.personal.project.dto.coupon.request.EventCouponIssueRequest;
import org.personal.project.dto.coupon.response.EventCouponIssueResponse;
import org.personal.project.service.coupon.CouponIssueService;
import org.personal.project.service.coupon.EventCouponIssueService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * 쿠폰 발급 API
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/coupons")
public class CouponIssueController {

    private final CouponIssueService couponIssueService;
    private final EventCouponIssueService eventCouponIssueService;

    /**
     * 일반 쿠폰 즉시 발급
     */
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @PostMapping("/{policyId}/issue")
    public CouponIssueResponse issue(@PathVariable Long policyId, Principal principal) {
        log.info("쿠폰 일반 발급 요청 policyId={}, memberId={}", policyId, principal.getName());

        CouponIssueResponse response = couponIssueService.issueCoupon(policyId, principal.getName());

        log.info("쿠폰 일반 발급 응답 policyId={}, memberId={}, memberCouponId={}, status={}",
                policyId, principal.getName(), response.memberCouponId(), response.status());

        return response;
    }

    /**
     * 이벤트 쿠폰 발급 요청 접수
     */
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @PostMapping("/events/{policyId}/issue")
    public EventCouponIssueResponse requestEventIssue(
            @PathVariable Long policyId,
            @Valid @RequestBody EventCouponIssueRequest request,
            Principal principal) {
        log.info("이벤트 쿠폰 발급 요청 policyId={}, memberId={}, requestKey={}",
                policyId, principal.getName(), request.requestKey());

        EventCouponIssueResponse response = eventCouponIssueService.requestIssue(
                policyId,
                principal.getName(),
                request.requestKey()
        );

        log.info("이벤트 쿠폰 발급 접수 응답 policyId={}, memberId={}, requestKey={}, status={}",
                policyId, principal.getName(), response.requestKey(), response.status());

        return response;
    }

    /**
     * 이벤트 쿠폰 발급 상태 조회
     */
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @GetMapping("/issues/{requestKey}")
    public CouponIssueStatusResponse getIssueStatus(@PathVariable String requestKey) {
        log.info("이벤트 쿠폰 발급 requestKey={}", requestKey);

        CouponIssueStatusResponse response = eventCouponIssueService.getIssueStatus(requestKey);

        log.info("이벤트 쿠폰 발급 상태 조회 응답 requestKey={}, status={}",
                response.requestKey(), response.status());

        return response;
    }
}
