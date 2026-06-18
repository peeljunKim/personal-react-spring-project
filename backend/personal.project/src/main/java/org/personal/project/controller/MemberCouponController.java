package org.personal.project.controller;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.personal.project.dto.coupon.response.CouponApplicabilityResponse;
import org.personal.project.dto.coupon.response.MemberCouponResponse;
import org.personal.project.dto.page.PageRequestDTO;
import org.personal.project.dto.page.PageResponseDTO;
import org.personal.project.entity.coupon.MemberCouponStatus;
import org.personal.project.service.coupon.MemberCouponQueryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

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
     * 내 쿠폰함 전체 조회
     */
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @GetMapping
    public PageResponseDTO<MemberCouponResponse> getMyCoupons(
            @RequestParam(required = false) MemberCouponStatus status,
            PageRequestDTO pageRequestDTO,
            Principal principal
    ) {
        return memberCouponQueryService.getMyCoupons(principal.getName(), status, pageRequestDTO);
    }

    /**
     * 사용 가능 쿠폰 조회
     */
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @GetMapping("/usable")
    public PageResponseDTO<MemberCouponResponse> getUsableCoupons(
            PageRequestDTO pageRequestDTO,
            Principal principal
    ) {
        return memberCouponQueryService.getUsableCoupons(principal.getName(), pageRequestDTO);
    }

    /**
     * 금액 기준 적용 가능 쿠폰 조회
     */
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @GetMapping("/applicable")
    public PageResponseDTO<CouponApplicabilityResponse> getApplicableCoupons(
            @RequestParam @PositiveOrZero Integer orderAmount,
            PageRequestDTO pageRequestDTO,
            Principal principal
    ) {
        return memberCouponQueryService.getApplicableCoupons(principal.getName(), orderAmount, pageRequestDTO);
    }

    /**
     * 쿠폰 사용 이력 조회(사용, 기간 만료, 취소)
     */
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @GetMapping("/history")
    public PageResponseDTO<MemberCouponResponse> getCouponHistory(
            PageRequestDTO pageRequestDTO,
            Principal principal
    ) {
        return memberCouponQueryService.getCouponHistory(principal.getName(), pageRequestDTO);
    }
}
