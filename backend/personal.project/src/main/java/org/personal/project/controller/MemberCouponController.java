package org.personal.project.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.personal.project.dto.coupon.response.CouponApplicabilityResponse;
import org.personal.project.dto.coupon.response.CouponPreviewResponse;
import org.personal.project.dto.coupon.response.MemberCouponResponse;
import org.personal.project.service.coupon.CouponApplyService;
import org.personal.project.service.coupon.MemberCouponQueryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

/**
 * 사용자 쿠폰함 API
 */
@RestController
@RequiredArgsConstructor
@Slf4j
//@Validated
@RequestMapping("/api/me/coupons")
public class MemberCouponController {

    private final MemberCouponQueryService memberCouponQueryService;
    private final CouponApplyService couponApplyService;

    /**
     * 지금 사용 가능 쿠폰 조회
     */
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @GetMapping
    public List<MemberCouponResponse> getMyCoupons(Principal principal) {
        log.info("내 쿠폰함 조회 요청 memberId={}", principal.getName());

        return memberCouponQueryService.getMyCoupons(principal.getName());
    }

    /**
     * 장바구니 기준 적용 가능 쿠폰 조회
     */
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @GetMapping("/applicable")
    public List<CouponApplicabilityResponse> getApplicableCoupons(
            Principal principal
    ) {
        log.info("장바구니 기준 적용 가능 쿠폰 조회 요청 memberId={}", principal.getName());

        return couponApplyService.getApplicableCoupons(principal.getName());
    }

    /**
     * 쿠폰 적용 된 금액 미리보기
     */
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @PostMapping("/{memberCouponId}/preview")
    public CouponPreviewResponse preview(
            @PathVariable Long memberCouponId,
            Principal principal
    ) {
        log.info("쿠폰 적용 된 금액 미리보기 요청 memberId={}, memberCouponId={}",
                principal.getName(), memberCouponId);

        return couponApplyService.preview(principal.getName(), memberCouponId);
    }
}
