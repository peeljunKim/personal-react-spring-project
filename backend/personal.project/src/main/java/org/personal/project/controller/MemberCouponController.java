package org.personal.project.controller;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.personal.project.dto.coupon.response.CouponApplicabilityResponse;
import org.personal.project.dto.coupon.response.MemberCouponResponse;
import org.personal.project.service.coupon.MemberCouponQueryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

/**
 * 사용자 쿠폰함 API
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
@RequestMapping("/api/me/coupons")
public class MemberCouponController {

    private final MemberCouponQueryService memberCouponQueryService;

    /**
     * 지금 사용 가능 쿠폰 조회
     */
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @GetMapping
    public List<MemberCouponResponse> getMyCoupons(Principal principal) {
        return memberCouponQueryService.getMyCoupons(principal.getName());
    }

    /**
     * 금액 기준 적용 가능 쿠폰 조회
     */
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @GetMapping("/applicable")
    public List<CouponApplicabilityResponse> getApplicableCoupons(
            @RequestParam @PositiveOrZero Integer orderAmount,
            Principal principal
    ) {
        return memberCouponQueryService.getApplicableCoupons(principal.getName(), orderAmount);
    }
}
