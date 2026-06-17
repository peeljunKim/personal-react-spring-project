package org.personal.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.personal.project.dto.coupon.request.CouponPolicyCreateRequest;
import org.personal.project.dto.coupon.response.CouponPolicyCreateResponse;
import org.personal.project.service.coupon.CouponPolicyCommandService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 쿠폰 API
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin/coupons")
public class CouponAdminController {

    private final CouponPolicyCommandService couponPolicyCommandService;

    /**
     * 쿠폰 정책 생성
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @PostMapping
    public CouponPolicyCreateResponse createPolicy(@Valid @RequestBody CouponPolicyCreateRequest request) {
        log.info("관리자 쿠폰 정책 생성 요청 name={}, issueType={}, applyScope={}",
                request.name(), request.issueType(), request.applyScope());

        CouponPolicyCreateResponse response = couponPolicyCommandService.createPolicy(request);

        log.info("관리자 쿠폰 정책 생성 완료 policyId={}, status={}", response.policyId(), response.status());
        return response;
    }
}
